package backend;

import backend.mips.MipsModule;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.text.MipsLsu;

import java.util.ArrayList;
import java.util.HashSet;

public class PeepHole {
    private final ArrayList<MipsAssembly> textSegment;

    public PeepHole() {
        MipsModule mipsModule = BackEnd.getMipsModule();
        this.textSegment = mipsModule.getTextSegment();
    }

    public void peep() {
        boolean finished = false;
        while (!finished) {
            finished = true;
            finished &= removeContinuousStores();
        }
    }

    private boolean removeContinuousStores() {
        boolean finished = true;
        HashSet<MipsAssembly> removeSet = new HashSet<>();
        for (int i = 0; i < textSegment.size(); i++) {
            MipsAssembly current = textSegment.get(i);
            if (current instanceof MipsLsu currentLsu && currentLsu.isStoreType()) {
                if (i == 0) {
                    continue;
                }
                MipsAssembly previous = textSegment.get(i - 1);
                if (previous instanceof MipsLsu previousLsu && previousLsu.isStoreType()) {
                    if (currentLsu.getTarget().equals(previousLsu.getTarget())) {
                        removeSet.add(previous);
                        finished = false;
                    }
                }
            }
        }

        textSegment.removeAll(removeSet);
        return finished;
    }
}
