package backend;

import backend.mips.MipsBuilder;
import backend.mips.MipsModule;
import midend.MidEnd;
import midend.llvm.IrModule;

public class BackEnd {
    private static IrModule irModule;
    private static MipsModule mipsModule;

    public static void generateMips() {
        mipsModule = new MipsModule();
        MipsBuilder.setMipsModule(mipsModule);

        irModule = MidEnd.getIrModule();
        irModule.toMips();
    }

    public static MipsModule getMipsModule() {
        return mipsModule;
    }
}
