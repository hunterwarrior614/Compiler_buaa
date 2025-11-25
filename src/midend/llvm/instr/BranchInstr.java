package midend.llvm.instr;

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
}
