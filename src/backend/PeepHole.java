package backend;

import backend.mips.MipsModule;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsLabel;
import backend.mips.assembly.text.MipsJump;
import backend.mips.assembly.text.MipsLsu;
import backend.mips.assembly.text.MipsAlu;
import backend.mips.Register;

import java.util.ArrayList;
import java.util.HashMap;
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
            finished = removeContinuousStores();
            finished &= removeJumpToNextLabel();
            finished &= flattenJumpChains();
            finished &= removeLoadAfterStoreSameReg();
            finished &= removeDuplicateConsecutiveLoads();
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

    private boolean flattenJumpChains() {
        boolean finished = true;

        // map label name to its index in the text segment
        HashMap<String, Integer> labelIndex = new HashMap<>();
        for (int i = 0; i < textSegment.size(); i++) {
            MipsAssembly assembly = textSegment.get(i);
            if (assembly instanceof MipsLabel label) {
                labelIndex.put(label.getLabel(), i);
            }
        }

        // find chains: j L1 where L1 immediately contains another unconditional j L2
        for (int i = 0; i < textSegment.size(); i++) {
            MipsAssembly assembly = textSegment.get(i);
            if (assembly instanceof MipsJump jump && jump.getJumpType() == MipsJump.JumpType.J) {
                String target = jump.getTargetLabel();
                Integer targetIdx = labelIndex.get(target);
                if (targetIdx != null) {
                    int nextIdx = targetIdx + 1;
                    if (nextIdx < textSegment.size()) {
                        MipsAssembly nextAssembly = textSegment.get(nextIdx);
                        if (nextAssembly instanceof MipsJump targetJump
                                && targetJump.getJumpType() == MipsJump.JumpType.J
                                && targetJump.getTargetLabel() != null) {
                            // redirect to final target
                            textSegment.set(i, new MipsJump(MipsJump.JumpType.J, targetJump.getTargetLabel()));
                            finished = false;
                        }
                    }
                }
            }
        }

        return finished;
    }

    private boolean removeJumpToNextLabel() {
        boolean finished = true;
        HashSet<MipsAssembly> removeSet = new HashSet<>();

        // map label name to its index in the text segment
        HashMap<String, Integer> labelIndex = new HashMap<>();
        for (int i = 0; i < textSegment.size(); i++) {
            MipsAssembly assembly = textSegment.get(i);
            if (assembly instanceof MipsLabel label) {
                labelIndex.put(label.getLabel(), i);
            }
        }

        for (int i = 0; i < textSegment.size(); i++) {
            MipsAssembly assembly = textSegment.get(i);
            if (assembly instanceof MipsJump jump && jump.getJumpType() == MipsJump.JumpType.J) {
                String target = jump.getTargetLabel();
                Integer targetIdx = labelIndex.get(target);
                if (targetIdx != null && targetIdx == i + 1) {
                    removeSet.add(assembly);
                    finished = false;
                }
            }
        }

        textSegment.removeAll(removeSet);
        return finished;
    }

    private boolean removeLoadAfterStoreSameReg() {
        boolean finished = true;
        HashSet<MipsAssembly> removeSet = new HashSet<>();

        for (int i = 1; i < textSegment.size(); i++) {
            MipsAssembly current = textSegment.get(i);
            MipsAssembly previous = textSegment.get(i - 1);

            if (current instanceof MipsLsu currentLsu && currentLsu.isLoadType()) {
                if (previous instanceof MipsLsu previousLsu && previousLsu.isStoreType()) {
                    if (currentLsu.getTarget().equals(previousLsu.getTarget())) {
                        if (currentLsu.getRd() == previousLsu.getRd()) {
                            removeSet.add(current);
                            finished = false;
                        } else {
                            // replace load with move from stored register to load dest
                            textSegment.set(i, new MipsAlu(
                                    MipsAlu.AluType.ADDU,
                                    currentLsu.getRd(), previousLsu.getRd(), Register.ZERO));
                            finished = false;
                        }
                    }
                }
            }
        }

        textSegment.removeAll(removeSet);
        return finished;
    }

    private boolean removeDuplicateConsecutiveLoads() {
        boolean finished = true;
        HashSet<MipsAssembly> removeSet = new HashSet<>();

        for (int i = 1; i < textSegment.size(); i++) {
            MipsAssembly current = textSegment.get(i);
            MipsAssembly previous = textSegment.get(i - 1);

            if (current instanceof MipsLsu currentLsu && currentLsu.isLoadType()) {
                if (previous instanceof MipsLsu previousLsu && previousLsu.isLoadType()) {
                    if (currentLsu.getTarget().equals(previousLsu.getTarget())
                            && currentLsu.getRd() == previousLsu.getRd()) {
                        removeSet.add(current);
                        finished = false;
                    } else if (currentLsu.getTarget().equals(previousLsu.getTarget())) {
                        // same address, different dest: turn into move
                        textSegment.set(i, new MipsAlu(
                                MipsAlu.AluType.ADDU,
                                currentLsu.getRd(), previousLsu.getRd(), Register.ZERO));
                        finished = false;
                    }
                }
            }
        }

        textSegment.removeAll(removeSet);
        return finished;
    }
}
