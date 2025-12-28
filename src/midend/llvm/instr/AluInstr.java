package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.text.MipsAlu;
import backend.mips.assembly.text.MipsMdu;
import midend.llvm.IrBuilder;
import midend.llvm.constant.IrConstInt;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;
import utils.Settings;

public class AluInstr extends IrInstr {
    public enum AluType {
        ADD,
        SUB,
        MUL,
        SDIV, // 有符号除法
        SREM, // 有符号取模
    }

    private final AluType aluType;
    private Register resultRegister;

    public AluInstr(String aluOp, IrValue lValue, IrValue rValue) {
        super(IrValueType.ALU_INSTR, new IrBaseType(IrBaseType.TypeValue.INT32), IrBuilder.getLocalVarName());
        this.aluType = translateAluType(aluOp);
        usees.add(lValue);
        usees.add(rValue);
    }

    private AluType translateAluType(String aluOp) {
        return switch (aluOp) {
            case "+" -> AluType.ADD;
            case "-" -> AluType.SUB;
            case "*" -> AluType.MUL;
            case "/" -> AluType.SDIV;
            case "%" -> AluType.SREM;
            default -> throw new IllegalArgumentException("[ERROR] Unknown aluOp]");
        };
    }

    private IrValue getLValue() {
        return usees.get(0);
    }

    private IrValue getRValue() {
        return usees.get(1);
    }

    @Override
    public String toString() {
        return name + " = " + aluType.toString().toLowerCase() + " i32 " + getLValue().getName() + ", "
                + getRValue().getName();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        IrValue lValue = getLValue();
        IrValue rValue = getRValue();

        Register lRegister = getRegisterOrK0ForIrValue(lValue);
        Register rRegister = getRegisterOrK1ForIrValue(rValue);
        // 为计算结果分配寄存器
        resultRegister = getRegisterOrK0ForIrValue(this);

        // 开启优化
        if (Settings.FINE_TUNING) {
            // 如果是乘除指令，则进行乘除法优化
            if (isMduInstr()) {
                switch (aluType) {
                    case MUL -> MulOptimize(lValue, rValue, lRegister, rRegister);
                    default -> {
                        loadIrValue2Register(lValue, lRegister);
                        loadIrValue2Register(rValue, rRegister);
                        generateMipsAlu(lRegister, rRegister);
                    }
                }
            }
            // 否则，如果右操作数是常数，则直接加减（省去常数的加载）
            // 注意：不可能出现两个常数相运算，否则已经被常数折叠
            else if (rValue instanceof IrConstInt irConstInt) {
                loadIrValue2Register(lValue, lRegister);
                generateMipsAlu(lRegister, irConstInt);
            } else {
                loadIrValue2Register(lValue, lRegister);
                loadIrValue2Register(rValue, rRegister);
                generateMipsAlu(lRegister, rRegister);
            }
        }
        // 关闭优化
        else {
            loadIrValue2Register(lValue, lRegister);
            loadIrValue2Register(rValue, rRegister);
            generateMipsAlu(lRegister, rRegister);
        }

        // 最后将结果保存到当前的irValue
        storeRegister2IrValue(resultRegister, this);
    }

    private boolean isMduInstr() {
        return aluType == AluType.MUL || aluType == AluType.SDIV || aluType == AluType.SREM;
    }

    private void generateMipsAlu(Register lRegister, Register rRegister) {
        switch (aluType) {
            case ADD -> new MipsAlu(MipsAlu.AluType.ADDU, resultRegister, lRegister, rRegister);
            case SUB -> new MipsAlu(MipsAlu.AluType.SUBU, resultRegister, lRegister, rRegister);
            case MUL -> {
                new MipsMdu(MipsMdu.MduType.MULT, lRegister, rRegister); // 计算
                new MipsMdu(MipsMdu.MduType.MFLO, resultRegister); // 取值
            }
            case SDIV -> {
                new MipsMdu(MipsMdu.MduType.DIV, lRegister, rRegister); // 计算
                new MipsMdu(MipsMdu.MduType.MFLO, resultRegister); // 取商
            }
            case SREM -> {
                new MipsMdu(MipsMdu.MduType.DIV, lRegister, rRegister); // 计算
                new MipsMdu(MipsMdu.MduType.MFHI, resultRegister); // 取模
            }
        }
    }

    private void generateMipsAlu(Register valueRegister, IrConstInt irConstInt) {
        int num = Integer.parseInt(irConstInt.getName());
        switch (aluType) {
            case ADD -> new MipsAlu(MipsAlu.AluType.ADDIU, resultRegister, valueRegister, num);
            case SUB -> new MipsAlu(MipsAlu.AluType.ADDIU, resultRegister, valueRegister, -num);
            default -> throw new IllegalArgumentException("[ERROR] Illegal aluOp");
        }
    }

    // 乘法优化
    // 1.如果两个操作数均为常数，则直接计算出结果
    // 2.如果仅有一个操作数为常数，则考虑进行乘法优化
    // 3.若没有常数，则生成原始指令
    private void MulOptimize(IrValue lValue, IrValue rValue, Register lRegister, Register rRegister) {
        boolean optimized = false;
        // 如果两个操作数均为常数，则直接计算出结果
        if (lValue instanceof IrConstInt && rValue instanceof IrConstInt) {
            int lNum = Integer.parseInt(lValue.getName());
            int rNum = Integer.parseInt(rValue.getName());
            int resNum = lNum * rNum;
            new MarsLi(resultRegister, resNum);
            optimized = true;
        }
        // 如果仅有一个操作数为常数，则考虑进行乘法优化
        else if (lValue instanceof IrConstInt irConstInt) {
            loadIrValue2Register(rValue, rRegister);
            optimized = MulOptimizeByShift(rValue, irConstInt, rRegister, false);
        } else if (rValue instanceof IrConstInt irConstInt) {
            loadIrValue2Register(lValue, lRegister);
            optimized = MulOptimizeByShift(lValue, irConstInt, lRegister, true);
        }

        // 否则，不优化
        if (!optimized) {
            loadIrValue2Register(lValue, lRegister);
            loadIrValue2Register(rValue, rRegister);
            generateMipsAlu(lRegister, rRegister);
        }
    }

    // 对于 x * const 考虑三种做法
    // 先将 const 取绝对值
    // 1. 设 N 为小于等于 const 的最大的二的幂，则有 1 条 SLL 指令与 (const-N) 条 ADDU 指令，共 const-N+1
    // 条指令，分数即指令数
    // 2. 设 N 为大于 const 的最小的二的幂，则有 1 条 SLL 指令与 (N-const) 条 SUBU 指令，共 N-const+1
    // 条指令，分数即指令数
    // 3. 1条原始乘指令，分数为 5
    // 取上述三种做法中分数最小的
    private boolean MulOptimizeByShift(IrValue value, IrConstInt irConstInt, Register registerValue, boolean k0) {
        resultRegister = k0 ? getRegisterOrK1ForIrValue(this) : getRegisterOrK0ForIrValue(this);
        int num = Integer.parseInt(irConstInt.getName());
        if (num == 0) {
            new MarsLi(resultRegister, 0);
            return true;
        } else if (num == 1) {
            loadIrValue2Register(value, resultRegister);
        } else if (num == -1) {
            loadIrValue2Register(value, resultRegister);
            new MipsAlu(MipsAlu.AluType.SUBU, resultRegister, Register.ZERO, resultRegister);
        }
        boolean negative = num < 0;
        num = Math.abs(num); // 常数先调为正值

        // 1. N <= num
        int n1 = Integer.highestOneBit(num);
        int diff1 = num - n1;
        int score1 = 1 + diff1;
        if (negative) {
            score1 += 1;
        }

        // 2. N > num
        int score2 = Integer.MAX_VALUE;
        int diff2 = 0;
        int n2 = n1;
        if (n2 < num && (n2 << 1) != 0) {
            n2 = n2 << 1;
            diff2 = n2 - num;
            score2 = 1 + diff2;
        }
        if (negative) {
            score2 += 1;
        }

        int score3 = 5;

        if (Math.min(score1, score2) > score3) {
            return false;
        }

        if (score1 <= score2) {
            int shift = Integer.numberOfTrailingZeros(n1);
            new MipsAlu(MipsAlu.AluType.SLL, resultRegister, registerValue, shift);
            for (int i = 0; i < diff1; i++) {
                new MipsAlu(MipsAlu.AluType.ADDU, resultRegister, resultRegister, registerValue);
            }
        } else {
            int shift = Integer.numberOfTrailingZeros(n2);
            new MipsAlu(MipsAlu.AluType.SLL, resultRegister, registerValue, shift);
            for (int i = 0; i < diff2; i++) {
                new MipsAlu(MipsAlu.AluType.SUBU, resultRegister, resultRegister, registerValue);
            }
        }

        if (negative) {
            new MipsAlu(MipsAlu.AluType.SUBU, resultRegister, Register.ZERO, resultRegister);
        }
        return true;
    }
}
