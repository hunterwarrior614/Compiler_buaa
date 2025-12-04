package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.text.MipsAlu;
import backend.mips.assembly.text.MipsMdu;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class AluInstr extends IrInstr {
    public enum AluType {
        ADD,
        SUB,
        MUL,
        SDIV,   // 有符号除法
        SREM,   // 有符号取模
    }

    private final AluType aluType;

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
        return name + " = " + aluType.toString().toLowerCase() + " i32 " + getLValue().getName() + ", " + getRValue().getName();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        IrValue lValue = getLValue();
        IrValue rValue = getRValue();

        Register lRegister = getRegisterOrK0ForIrValue(lValue);
        Register rRegister = getRegisterOrK1ForIrValue(rValue);
        // 为计算结果分配寄存器
        Register resultRegister = getRegisterOrK0ForIrValue(this);

        loadIrValue2Register(lValue, lRegister);
        loadIrValue2Register(rValue, rRegister);

        switch (aluType) {
            case ADD -> new MipsAlu(MipsAlu.AluType.ADD, resultRegister, lRegister, rRegister);
            case SUB -> new MipsAlu(MipsAlu.AluType.SUB, resultRegister, lRegister, rRegister);
            case MUL -> {
                new MipsMdu(MipsMdu.MduType.MULT, lRegister, rRegister);    // 计算
                new MipsMdu(MipsMdu.MduType.MFLO, resultRegister);   // 取值
            }
            case SDIV -> {
                new MipsMdu(MipsMdu.MduType.DIV, lRegister, rRegister); // 计算
                new MipsMdu(MipsMdu.MduType.MFLO, resultRegister);  // 取商
            }
            case SREM -> {
                new MipsMdu(MipsMdu.MduType.DIV, lRegister, rRegister); // 计算
                new MipsMdu(MipsMdu.MduType.MFHI, resultRegister);  // 取模
            }
        }
        // 最后将结果保存到当前的irValue
        storeRegister2IrValue(resultRegister, this);
    }
}
