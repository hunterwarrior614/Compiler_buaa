package optimize;

import midend.llvm.instr.*;
import midend.llvm.instr.io.IOInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.*;

public class RemoveDeadCode extends Optimizer {
    private final Map<IrFunc, Set<IrFunc>> callGraph;
    private final Map<IrFunc, Set<IrFunc>> reverseCallGraph;
    private final Set<IrFunc> sideEffectFuncs;

    public RemoveDeadCode() {
        this.callGraph = new HashMap<>();
        this.reverseCallGraph = new HashMap<>();
        this.sideEffectFuncs = new HashSet<>();
    }

    @Override
    public void Optimize() {
        boolean changed = true;
        while (changed) {
            changed = false;
            this.analyzeCallGraph();
            changed |= this.eliminateUnusedFunctions();
            changed |= this.eliminateUnreachableBlocks();
            changed |= this.eliminateRedundantStores();
            changed |= this.eliminateDeadInstructions();
            changed |= this.simplifyPhis();
            changed |= this.simplifyBranches();
            changed |= this.mergeBasicBlocks();
        }
    }

    private boolean eliminateRedundantStores() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                Map<IrValue, StoreInstr> lastStores = new HashMap<>();
                Set<IrInstr> deadStores = new HashSet<>();

                for (IrInstr instr : block.getInstrs()) {
                    if (instr instanceof StoreInstr store) {
                        IrValue addr = store.getAddress();
                        if (lastStores.containsKey(addr)) {
                            deadStores.add(lastStores.get(addr));
                            changed = true;
                        }
                        lastStores.put(addr, store);
                    } else if (instr instanceof LoadInstr || instr instanceof CallInstr || instr instanceof IOInstr) {
                        lastStores.clear();
                    }
                }

                if (!deadStores.isEmpty()) {
                    Iterator<IrInstr> it = block.getInstrs().iterator();
                    while (it.hasNext()) {
                        IrInstr instr = it.next();
                        if (deadStores.contains(instr)) {
                            instr.removeAllUsees();
                            it.remove();
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean simplifyBranches() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                if (block.getInstrs().isEmpty())
                    continue;
                IrInstr last = block.getInstrs().get(block.getInstrs().size() - 1);
                if (last instanceof BranchInstr branch) {
                    if (branch.getTrueBlock() == branch.getFalseBlock()) {
                        JumpInstr jump = new JumpInstr(branch.getTrueBlock(), block);
                        block.getInstrs().set(block.getInstrs().size() - 1, jump);
                        branch.removeAllUsees();
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private void analyzeCallGraph() {
        callGraph.clear();
        reverseCallGraph.clear();
        sideEffectFuncs.clear();

        for (IrFunc func : irModule.getIrFuncs()) {
            callGraph.put(func, new HashSet<>());
            reverseCallGraph.put(func, new HashSet<>());
        }

        // 1. Initialize local side effects and build graph
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                for (IrInstr instr : block.getInstrs()) {
                    if (instr instanceof CallInstr call) {
                        IrFunc callee = call.getFunc();
                        callGraph.get(func).add(callee);
                        reverseCallGraph.get(callee).add(func);
                    } else if (instr instanceof StoreInstr || instr instanceof IOInstr) {
                        sideEffectFuncs.add(func);
                    }
                }
            }
        }

        // 2. Propagate side effects
        boolean changed = true;
        while (changed) {
            changed = false;
            for (IrFunc func : irModule.getIrFuncs()) {
                if (!sideEffectFuncs.contains(func)) {
                    for (IrFunc callee : callGraph.get(func)) {
                        if (sideEffectFuncs.contains(callee)) {
                            sideEffectFuncs.add(func);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean eliminateUnusedFunctions() {
        boolean changed = false;
        Iterator<IrFunc> it = irModule.getIrFuncs().iterator();
        while (it.hasNext()) {
            IrFunc func = it.next();
            if (func.isMainFunction())
                continue;

            if (reverseCallGraph.get(func).isEmpty()) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    private boolean eliminateUnreachableBlocks() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            Iterator<IrBasicBlock> it = func.getBasicBlocks().iterator();
            while (it.hasNext()) {
                IrBasicBlock block = it.next();
                if (block.isEntryBlock())
                    continue;

                if (block.getBeforeBlocks().isEmpty()) {
                    for (IrBasicBlock succ : block.getNextBlocks()) {
                        succ.getBeforeBlocks().remove(block);
                        for (IrInstr instr : succ.getInstrs()) {
                            if (instr instanceof PhiInstr phi) {
                                phi.removeBlock(block);
                            }
                        }
                    }

                    for (IrInstr instr : block.getInstrs()) {
                        instr.removeAllUsees();
                    }

                    it.remove();
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean eliminateDeadInstructions() {
        Set<IrInstr> liveInstrs = markLiveInstructions();
        boolean changed = false;

        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                Iterator<IrInstr> it = block.getInstrs().iterator();
                while (it.hasNext()) {
                    IrInstr instr = it.next();
                    if (!liveInstrs.contains(instr)) {
                        instr.removeAllUsees();
                        it.remove();
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private Set<IrInstr> markLiveInstructions() {
        Set<IrInstr> live = new HashSet<>();
        Queue<IrInstr> workList = new LinkedList<>();

        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                for (IrInstr instr : block.getInstrs()) {
                    if (isCritical(instr)) {
                        live.add(instr);
                        workList.add(instr);
                    }
                }
            }
        }

        while (!workList.isEmpty()) {
            IrInstr instr = workList.poll();
            for (IrValue op : instr.getUsees()) {
                if (op instanceof IrInstr opInstr && !live.contains(opInstr)) {
                    live.add(opInstr);
                    workList.add(opInstr);
                }
            }
        }
        return live;
    }

    private boolean isCritical(IrInstr instr) {
        if (instr instanceof ReturnInstr ||
                instr instanceof BranchInstr ||
                instr instanceof JumpInstr ||
                instr instanceof StoreInstr ||
                instr instanceof IOInstr) {
            return true;
        }
        if (instr instanceof CallInstr call) {
            return sideEffectFuncs.contains(call.getFunc());
        }
        return false;
    }

    private boolean simplifyPhis() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                Iterator<IrInstr> it = block.getInstrs().iterator();
                while (it.hasNext()) {
                    IrInstr instr = it.next();
                    if (instr instanceof PhiInstr phi) {
                        ArrayList<IrValue> incoming = phi.getUsees();
                        if (incoming.size() == 1) {
                            phi.replaceAllUsesWith(incoming.get(0));
                            phi.removeAllUsees();
                            it.remove();
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean mergeBasicBlocks() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            Iterator<IrBasicBlock> it = func.getBasicBlocks().iterator();
            while (it.hasNext()) {
                IrBasicBlock block = it.next();
                if (canMerge(block)) {
                    IrBasicBlock pred = block.getBeforeBlocks().get(0);
                    pred.appendBlock(block);
                    it.remove();
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean canMerge(IrBasicBlock block) {
        if (block.getBeforeBlocks().size() != 1)
            return false;
        IrBasicBlock pred = block.getBeforeBlocks().get(0);
        return pred.getNextBlocks().size() == 1 && pred.getNextBlocks().get(0) == block;
    }
}
