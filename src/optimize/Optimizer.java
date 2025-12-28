package optimize;

import midend.llvm.IrModule;

public abstract class Optimizer {
    protected static IrModule irModule;

    public abstract void Optimize();

    public static void setIrModule(IrModule irModule) {
        Optimizer.irModule = irModule;
    }
}
