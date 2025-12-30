package backend;

import backend.mips.MipsBuilder;
import backend.mips.MipsModule;
import midend.MidEnd;
import midend.llvm.IrModule;
import utils.Settings;

public class BackEnd {
    private static MipsModule mipsModule;

    public static void generateMips() {
        mipsModule = new MipsModule();
        MipsBuilder.setMipsModule(mipsModule);

        IrModule irModule = MidEnd.getIrModule();
        irModule.toMips();

        if (Settings.FINE_TUNING) {
            PeepHole peepHole = new PeepHole();
            peepHole.peep();
        }
    }

    public static MipsModule getMipsModule() {
        return mipsModule;
    }
}
