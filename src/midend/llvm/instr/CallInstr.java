package midend.llvm.instr;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.text.MipsAlu;
import backend.mips.assembly.text.MipsJump;
import backend.mips.assembly.text.MipsLsu;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.ArrayList;

public class CallInstr extends IrInstr {
    public CallInstr(IrFunc func, ArrayList<IrValue> params) {
        super(IrValueType.CALL_INSTR, new IrBaseType(func.getIrBaseTypeValue()),
                // 若是 void 函数，直接调用（call ...），否则要有变量接收（%n = call ...）
                func.getIrBaseTypeValue().equals(IrBaseType.TypeValue.VOID) ? "call" : IrBuilder.getLocalVarName()
        );
        addUsee(func);
        params.forEach(this::addUsee);
    }

    private IrFunc getFunc() {
        return (IrFunc) usees.get(0);
    }

    private ArrayList<IrValue> getParams() {
        return new ArrayList<>(usees.subList(1, usees.size()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 非 void 调用要有值
        if (!name.equals("call")) {
            sb.append(name).append(" = ");
        }

        IrFunc func = getFunc();
        sb.append("call ").append(func.getReturnTypeString()).append(" ").append(func.getName());
        sb.append("(");
        // 参数
        ArrayList<IrValue> params = getParams();
        ArrayList<String> paramStrings = new ArrayList<>();
        for (IrValue param : params) {
            paramStrings.add(param.getIrBaseType() + " " + param.getName());
        }
        sb.append(String.join(", ", paramStrings));

        sb.append(")");
        return sb.toString();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        对于函数调用者，主要有以下几个步骤：
        1. 保存现场
        2. 参数传递
        3. 函数跳转
        4. 恢复现场
         */
        ArrayList<Register> allocatedRegisters = MipsBuilder.getCurrentAllocatedRegisters();    // 现场信息

        saveContext(allocatedRegisters);    // 保存现场
        passParams();   // 参数传递
        jumpToFunction();   // 函数跳转
        recoverContext(allocatedRegisters);

        // 处理返回值
        if (!name.equals("call")) {
            storeRegister2IrValue(Register.V0, this);
        }
    }

    // 保存现场
    private void saveContext(ArrayList<Register> allocatedRegisters) {
        int currentStackOffset = MipsBuilder.getCurrentStackOffset();
        // 将所有分配的寄存器全部保存到当前栈中
        int registerCount = 0;
        for (Register register : allocatedRegisters) {
            registerCount++;
            new MipsLsu(MipsLsu.LsuType.SW, register, Register.SP, currentStackOffset - 4 * registerCount);
        }
        // 保存 $sp 和 $ra
        new MipsLsu(MipsLsu.LsuType.SW, Register.SP, Register.SP, currentStackOffset - 4 * registerCount - 4);
        new MipsLsu(MipsLsu.LsuType.SW, Register.RA, Register.SP, currentStackOffset - 4 * registerCount - 8);
        MipsBuilder.allocateStackSpace(4 * registerCount + 8);
    }

    // 参数传递
    private void passParams() {
        // 对于 MIPS，可以将前四个参数通过 $a0 - $a3 四个寄存器传递，但仍需要为其在栈中预留位置
        ArrayList<IrValue> params = getParams();
        for (int i = 0; i < params.size(); i++) {
            if (i < 3) {
                Register paramRegister = Register.getRegister(Register.A0.ordinal() + i);
                loadIrValue2Register(params.get(i), paramRegister);
                // MipsBuilder.allocateStackSpace(4);  // 在栈中预留位置
            } else {
                // 其余的参数压入栈
                loadIrValue2Register(params.get(i), Register.K0);
                new MipsLsu(MipsLsu.LsuType.SW, Register.K0, Register.SP, MipsBuilder.getCurrentStackOffset() - 4);
                // MipsBuilder.allocateStackSpace(4);
            }
        }

        // 计算出新的 $sp（整个函数只在此处移动一次栈）
        new MipsAlu(MipsAlu.AluType.ADDI, Register.SP, Register.SP, MipsBuilder.getCurrentStackOffset());
    }

    // 函数跳转
    private void jumpToFunction() {
        new MipsJump(MipsJump.JumpType.JAL, getFunc().getOriginName());
    }

    // 恢复现场
    private void recoverContext(ArrayList<Register> allocatedRegisters) {
        // 从栈顶逆序恢复寄存器
        // 先恢复 $ra 和 $sp
        new MipsLsu(MipsLsu.LsuType.LW, Register.RA, Register.SP, 0);
        new MipsLsu(MipsLsu.LsuType.LW, Register.SP, Register.SP, 4);
        // 再恢复分配的寄存器（注意此时 $sp 已经恢复）
        int currentStackOffset = MipsBuilder.getCurrentStackOffset();
        int registerCount = 0;
        for (int i = allocatedRegisters.size() - 1; i >= 0; i--) {
            registerCount++;
            new MipsLsu(MipsLsu.LsuType.LW, allocatedRegisters.get(i), Register.SP, currentStackOffset + 4 + registerCount * 4);
        }
        // TODO:释放栈？    MipsBuilder.allocateStackSpace(-(4 * registerCount + 8));
    }
}
