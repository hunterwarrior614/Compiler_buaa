package optimize;

import midend.MidEnd;

import java.util.ArrayList;

public class OptimizeManager {
    private static ArrayList<Optimizer> optimizerList;

    public static void Init() {
        Optimizer.setIrModule(MidEnd.getIrModule());

        optimizerList = new ArrayList<>();

        optimizerList.add(new RemoveUnreachCode());
        optimizerList.add(new CfgBuilder());
        optimizerList.add(new RemoveDeadCode());
        optimizerList.add(new CfgBuilder());

        optimizerList.add(new MemToReg());
        optimizerList.add(new CfgBuilder());

        optimizerList.add(new RemoveUnreachCode());
        optimizerList.add(new CfgBuilder());
        optimizerList.add(new RemoveDeadCode());
        optimizerList.add(new CfgBuilder());

        optimizerList.add(new RemovePhi());
    }

    public static void Optimize() {
        for (Optimizer optimizer : optimizerList) {
            optimizer.Optimize();
        }
    }
}
