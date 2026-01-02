package optimize;

import midend.MidEnd;
import midend.llvm.value.IrFunc;

public class AllocateRegister extends Optimizer {
    @Override
    public void Optimize() {
        for (IrFunc func : MidEnd.getIrModule().getIrFuncs()) {
            RegisterAllocator allocator = new RegisterAllocator(func);
            if (!func.getBasicBlocks().isEmpty()) {
                allocator.run(func.getBasicBlocks().get(0));
            }
        }
    }
}
