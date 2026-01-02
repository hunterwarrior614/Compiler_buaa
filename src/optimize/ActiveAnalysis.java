package optimize;

import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrGlobalVariable;
import midend.llvm.value.IrParameter;
import midend.llvm.value.IrValue;

import java.util.*;

public class ActiveAnalysis extends Optimizer {
    @Override
    public void Optimize() {
        this.resetLivenessInfo();
        this.computeDefUse();
        this.computeLiveInOut();
    }

    private void resetLivenessInfo() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                block.clearActiveInfo();
            }
        }
    }

    private void computeDefUse() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                HashSet<IrValue> defs = block.getDefValueSet();
                HashSet<IrValue> uses = block.getUseValueSet();

                for (IrInstr instr : block.getInstrs()) {
                    // Handle Uses
                    for (IrValue val : instr.getUsees()) {
                        if (instr instanceof MoveInstr move && val == move.getDstValue()) {
                            continue;
                        }

                        if (isVariable(val) && !defs.contains(val)) {
                            uses.add(val);
                        }
                    }

                    // Handle Defs
                    if (instr instanceof MoveInstr move) {
                        defs.add(move.getDstValue());
                    } else if (!instr.getIrBaseType().isVoid()) {
                        defs.add(instr);
                    }
                }
            }
        }
    }

    private boolean isVariable(IrValue val) {
        return val instanceof IrInstr || val instanceof IrParameter || val instanceof IrGlobalVariable;
    }

    private void computeLiveInOut() {
        for (IrFunc func : irModule.getIrFuncs()) {
            if (func.getBasicBlocks().isEmpty())
                continue;

            Queue<IrBasicBlock> workList = new LinkedList<>(func.getBasicBlocks());
            Set<IrBasicBlock> inQueue = new HashSet<>(func.getBasicBlocks());

            while (!workList.isEmpty()) {
                IrBasicBlock block = workList.poll();
                inQueue.remove(block);

                HashSet<IrValue> newOut = new HashSet<>();
                for (IrBasicBlock succ : block.getNextBlocks()) {
                    newOut.addAll(succ.getInValueSet());
                }

                HashSet<IrValue> newIn = new HashSet<>(newOut);
                newIn.removeAll(block.getDefValueSet());
                newIn.addAll(block.getUseValueSet());

                if (!newIn.equals(block.getInValueSet()) || !newOut.equals(block.getOutValueSet())) {
                    block.setInValueSet(newIn);
                    block.setOutValueSet(newOut);

                    for (IrBasicBlock pred : block.getBeforeBlocks()) {
                        if (!inQueue.contains(pred)) {
                            workList.add(pred);
                            inQueue.add(pred);
                        }
                    }
                }
            }
        }
    }
}
