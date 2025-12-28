package optimize;

import midend.llvm.instr.AllocateInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;

import java.util.ArrayList;

public class MemToReg extends Optimizer {
    @Override
    public void Optimize() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
//            if (irFunction.getBasicBlocks().isEmpty())
//                continue;
            IrBasicBlock entryBlock = irFunction.getBasicBlocks().get(0);
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                ArrayList<IrInstr> instrList = new ArrayList<>(irBasicBlock.getInstrs());
                for (IrInstr instr : instrList) {
                    if (isValueAllocate(instr)) {
                        InsertPhi insertPhi = new InsertPhi((AllocateInstr) instr, entryBlock);
                        insertPhi.addPhi();
                    }
                }
            }
        }
    }

    private boolean isValueAllocate(IrInstr instr) {
        // 只对非数组类型添加phi
        if (instr instanceof AllocateInstr allocateInstr) {
            IrBaseType targetType = allocateInstr.getIrBaseType().getPointValueType();
            return !targetType.getTypeValue().equals(IrBaseType.TypeValue.INT_ARRAY);
        }
        return false;
    }
}
