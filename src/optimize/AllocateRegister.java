package optimize;

import midend.MidEnd;
import midend.llvm.value.IrFunc;

public class AllocateRegister extends Optimizer {
    @Override
    public void Optimize() {
        for (IrFunc irFunction : MidEnd.getIrModule().getIrFuncs()) {
            RegisterAllocator allocator = new RegisterAllocator(irFunction);
            // 从起始开始分配
            allocator.Allocate(irFunction.getBasicBlocks().get(0));
        }
    }
}
