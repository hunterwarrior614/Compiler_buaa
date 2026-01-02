package optimize;

import midend.llvm.instr.AllocateInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;

import java.util.ArrayList;
import java.util.List;

public class MemToReg extends Optimizer {
    @Override
    public void Optimize() {
        for (IrFunc func : irModule.getIrFuncs()) {
            promoteMemoryToRegister(func);
        }
    }

    private void promoteMemoryToRegister(IrFunc func) {
        if (func.getBasicBlocks().isEmpty())
            return;

        IrBasicBlock entry = func.getBasicBlocks().get(0);
        List<AllocateInstr> allocas = findAllocas(func);

        for (AllocateInstr alloca : allocas) {
            InsertPhi transformer = new InsertPhi(alloca, entry);
            transformer.addPhi();
        }
    }

    private List<AllocateInstr> findAllocas(IrFunc func) {
        List<AllocateInstr> list = new ArrayList<>();
        for (IrBasicBlock block : func.getBasicBlocks()) {
            for (IrInstr instr : block.getInstrs()) {
                if (canPromote(instr)) {
                    list.add((AllocateInstr) instr);
                }
            }
        }
        return list;
    }

    private boolean canPromote(IrInstr instr) {
        if (instr instanceof AllocateInstr alloca) {
            return !alloca.getIrBaseType().getPointValueType().getTypeValue().equals(IrBaseType.TypeValue.INT_ARRAY);
        }
        return false;
    }
}
