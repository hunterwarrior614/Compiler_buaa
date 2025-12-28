package midend.llvm.instr.phi;

import midend.llvm.instr.IrInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrValue;

import java.util.ArrayList;

public class ParallelCopyInstr extends IrInstr {
    private final ArrayList<IrValue> srcList;
    private final ArrayList<IrValue> dstList;

    public ParallelCopyInstr(IrBasicBlock irBasicBlock) {
        super(IrValueType.PCOPY_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "parallel-copy", false);
        srcList = new ArrayList<>();
        dstList = new ArrayList<>();
        setIrBasicBlock(irBasicBlock);
    }

    public void addCopy(IrValue src, IrValue dst) {
        srcList.add(src);
        dstList.add(dst);
    }

    public ArrayList<IrValue> getSrcList() {
        return srcList;
    }

    public ArrayList<IrValue> getDstList() {
        return dstList;
    }

    @Override
    public String toString() {
        return "parallel-copy-instr";
    }

    @Override
    public void toMips() {
        throw new RuntimeException("[ERROR] pcopy should not delete!");
    }
}
