package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.text.MipsBranch;
import backend.mips.assembly.text.MipsJump;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrValue;

public class BranchInstr extends IrInstr {
    public BranchInstr(IrValue cond, IrBasicBlock trueBlock, IrBasicBlock falseBlock) {
        super(IrValueType.BRANCH_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "branch");
        usees.add(cond);
        usees.add(trueBlock);
        usees.add(falseBlock);
    }

    private IrValue getCond() {
        return usees.get(0);
    }

    private IrValue getTrueBlock() {
        return usees.get(1);
    }

    private IrValue getFalseBlock() {
        return usees.get(2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("br i1 ").append(getCond().getName());
        sb.append(", label %").append(getTrueBlock().getName());
        sb.append(", label %").append(getFalseBlock().getName());
        return sb.toString();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        bne $t1,$zero,label1    -> 条件为真，跳转到 label1
        j label2                -> 否则跳转到 label2
         */
        // 使用bne指令判断是否跳转到trueBlock：
        IrValue cond = getCond();
        Register condRegister = getRegisterOrK0ForIrValue(cond);
        new MipsBranch(MipsBranch.BranchType.BNE, condRegister, Register.ZERO, getTrueBlock().getName());
        // 若不跳转（执行到此处），则直接跳转到falseBlock
        new MipsJump(MipsJump.JumpType.J, getFalseBlock().getName());
    }
}
