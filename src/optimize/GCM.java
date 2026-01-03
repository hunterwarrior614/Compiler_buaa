package optimize;

import midend.llvm.instr.*;
import midend.llvm.instr.io.IOInstr;
import midend.llvm.instr.phi.ParallelCopyInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.use.IrUse;
import midend.llvm.use.IrUser;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.*;

public class GCM extends Optimizer {
    private Map<IrBasicBlock, Integer> loopDepthMap;
    private Set<IrInstr> pinnedInstrs;
    private Map<IrInstr, IrBasicBlock> earlyBlock;
    private Map<IrInstr, IrBasicBlock> lateBlock;
    private Map<IrInstr, IrBasicBlock> bestBlock;
    private Set<IrInstr> visitedEarly;
    private Set<IrInstr> visitedLate;

    @Override
    public void Optimize() {
        new CfgBuilder().Optimize(); // Ensure Dominators are up to date
        for (IrFunc func : irModule.getIrFuncs()) {
            if (func.getBasicBlocks().isEmpty())
                continue;
            runGCM(func);
        }
    }

    private void runGCM(IrFunc func) {
        loopDepthMap = new HashMap<>();
        pinnedInstrs = new HashSet<>();
        earlyBlock = new HashMap<>();
        lateBlock = new HashMap<>();
        bestBlock = new HashMap<>();
        visitedEarly = new HashSet<>();
        visitedLate = new HashSet<>();

        computeLoopDepth(func);
        findPinnedInsts(func);

        List<IrInstr> allInstrs = new ArrayList<>();
        for (IrBasicBlock block : func.getBasicBlocks()) {
            allInstrs.addAll(block.getInstrs());
        }

        IrBasicBlock entry = func.getBasicBlocks().get(0);
        for (IrInstr instr : allInstrs) {
            scheduleEarly(instr, entry);
        }

        for (IrInstr instr : allInstrs) {
            scheduleLate(instr);
        }

        for (IrInstr instr : allInstrs) {
            selectBlock(instr);
        }

        moveInstructions(func, allInstrs);
    }

    private void computeLoopDepth(IrFunc func) {
        for (IrBasicBlock block : func.getBasicBlocks()) {
            loopDepthMap.put(block, 0);
        }

        for (IrBasicBlock u : func.getBasicBlocks()) {
            for (IrBasicBlock v : u.getBeforeBlocks()) {
                if (v.getDominatorBlocks().contains(u)) {
                    Set<IrBasicBlock> loopNodes = new HashSet<>();
                    loopNodes.add(u);
                    loopNodes.add(v);

                    Queue<IrBasicBlock> queue = new LinkedList<>();
                    queue.add(v);

                    while (!queue.isEmpty()) {
                        IrBasicBlock curr = queue.poll();
                        for (IrBasicBlock pred : curr.getBeforeBlocks()) {
                            if (!loopNodes.contains(pred)) {
                                loopNodes.add(pred);
                                queue.add(pred);
                            }
                        }
                    }

                    for (IrBasicBlock block : loopNodes) {
                        loopDepthMap.put(block, loopDepthMap.get(block) + 1);
                    }
                }
            }
        }
    }

    private void findPinnedInsts(IrFunc func) {
        for (IrBasicBlock block : func.getBasicBlocks()) {
            for (IrInstr instr : block.getInstrs()) {
                if (instr instanceof BranchInstr ||
                        instr instanceof JumpInstr ||
                        instr instanceof ReturnInstr ||
                        instr instanceof StoreInstr ||
                        instr instanceof CallInstr ||
                        instr instanceof PhiInstr ||
                        instr instanceof LoadInstr ||
                        instr instanceof AllocateInstr ||
                        instr instanceof IOInstr ||
                        instr instanceof MoveInstr ||
                        instr instanceof ParallelCopyInstr) {
                    pinnedInstrs.add(instr);
                }
            }
        }
    }

    private void scheduleEarly(IrInstr instr, IrBasicBlock entry) {
        if (visitedEarly.contains(instr))
            return;
        visitedEarly.add(instr);

        earlyBlock.put(instr, entry);

        if (pinnedInstrs.contains(instr)) {
            earlyBlock.put(instr, instr.getIrBasicBlock());
            return;
        }

        IrBasicBlock root = entry;
        for (IrValue op : instr.getUsees()) {
            if (op instanceof IrInstr opInstr) {
                scheduleEarly(opInstr, entry);
                IrBasicBlock opEarly = earlyBlock.get(opInstr);
                root = deepestDom(root, opEarly);
            }
        }
        earlyBlock.put(instr, root);
    }

    private IrBasicBlock deepestDom(IrBasicBlock a, IrBasicBlock b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        if (b.getDominatorBlocks().contains(a))
            return b;
        if (a.getDominatorBlocks().contains(b))
            return a;
        return b;
    }

    private void scheduleLate(IrInstr instr) {
        if (visitedLate.contains(instr))
            return;
        visitedLate.add(instr);

        if (pinnedInstrs.contains(instr)) {
            lateBlock.put(instr, instr.getIrBasicBlock());
            return;
        }

        IrBasicBlock lca = null;
        for (IrUse use : instr.getUseList()) {
            IrUser user = use.getUser();
            if (user instanceof IrInstr userInstr) {
                scheduleLate(userInstr);
                IrBasicBlock useBlock = lateBlock.get(userInstr);

                if (userInstr instanceof PhiInstr phi) {
                    ArrayList<IrBasicBlock> preds = phi.getBeforeBlocks();
                    ArrayList<IrValue> values = phi.getUsees();
                    for (int i = 0; i < values.size(); i++) {
                        if (values.get(i) == instr) {
                            IrBasicBlock pred = preds.get(i);
                            lca = findLCA(lca, pred);
                        }
                    }
                } else {
                    lca = findLCA(lca, useBlock);
                }
            }
        }
        lateBlock.put(instr, lca);
    }

    private IrBasicBlock findLCA(IrBasicBlock a, IrBasicBlock b) {
        if (a == null)
            return b;
        if (b == null)
            return a;

        if (b.getDominatorBlocks().contains(a))
            return a;
        if (a.getDominatorBlocks().contains(b))
            return b;

        IrBasicBlock curr = a;
        while (curr != null) {
            if (b.getDominatorBlocks().contains(curr)) {
                return curr;
            }
            curr = curr.getImmediateDominator();
        }
        return null;
    }

    private void selectBlock(IrInstr instr) {
        if (pinnedInstrs.contains(instr)) {
            bestBlock.put(instr, instr.getIrBasicBlock());
            return;
        }

        IrBasicBlock best = lateBlock.get(instr);
        IrBasicBlock early = earlyBlock.get(instr);
        IrBasicBlock curr = best;

        while (curr != early && curr != null) {
            curr = curr.getImmediateDominator();
            if (curr == null)
                break;

            if (loopDepthMap.getOrDefault(curr, 0) < loopDepthMap.getOrDefault(best, 0)) {
                best = curr;
            }
            if (curr == early)
                break;
        }
        bestBlock.put(instr, best);
    }

    private void moveInstructions(IrFunc func, List<IrInstr> allInstrs) {
        List<IrInstr> floating = new ArrayList<>();
        for (IrInstr instr : allInstrs) {
            if (!pinnedInstrs.contains(instr)) {
                floating.add(instr);
            }
        }

        for (IrInstr instr : floating) {
            instr.getIrBasicBlock().getInstrs().remove(instr);
        }

        Map<IrBasicBlock, List<IrInstr>> targetMap = new HashMap<>();
        for (IrInstr instr : floating) {
            IrBasicBlock target = bestBlock.get(instr);
            if (target != null) {
                targetMap.computeIfAbsent(target, k -> new ArrayList<>()).add(instr);
            }
        }

        for (Map.Entry<IrBasicBlock, List<IrInstr>> entry : targetMap.entrySet()) {
            IrBasicBlock block = entry.getKey();
            List<IrInstr> toInsert = entry.getValue();
            List<IrInstr> sorted = topologicalSort(toInsert);

            for (IrInstr instr : sorted) {
                insertInstruction(block, instr);
            }
        }
    }

    private List<IrInstr> topologicalSort(List<IrInstr> instrs) {
        List<IrInstr> result = new ArrayList<>();
        Set<IrInstr> visited = new HashSet<>();
        Set<IrInstr> temp = new HashSet<>();

        for (IrInstr instr : instrs) {
            visitTopo(instr, instrs, visited, temp, result);
        }
        return result;
    }

    private void visitTopo(IrInstr instr, List<IrInstr> scope, Set<IrInstr> visited, Set<IrInstr> temp,
            List<IrInstr> result) {
        if (visited.contains(instr))
            return;
        if (temp.contains(instr))
            return;

        temp.add(instr);
        for (IrValue op : instr.getUsees()) {
            if (op instanceof IrInstr opInstr && scope.contains(opInstr)) {
                visitTopo(opInstr, scope, visited, temp, result);
            }
        }
        temp.remove(instr);
        visited.add(instr);
        result.add(instr);
    }

    private void insertInstruction(IrBasicBlock block, IrInstr instr) {
        int startIndex = 0;
        List<IrInstr> blockInstrs = block.getInstrs();
        while (startIndex < blockInstrs.size() && blockInstrs.get(startIndex) instanceof PhiInstr) {
            startIndex++;
        }

        int minIndex = startIndex;
        int maxIndex = blockInstrs.size();

        // Constraint 1: Must be after all operands defined in this block
        for (IrValue op : instr.getUsees()) {
            if (op instanceof IrInstr opInstr && opInstr.getIrBasicBlock() == block) {
                int idx = blockInstrs.indexOf(opInstr);
                if (idx >= 0) {
                    minIndex = Math.max(minIndex, idx + 1);
                }
            }
        }

        // Constraint 2: Must be before all users in this block
        for (IrUse use : instr.getUseList()) {
            IrUser user = use.getUser();
            if (user instanceof IrInstr userInstr && userInstr.getIrBasicBlock() == block) {
                int idx = blockInstrs.indexOf(userInstr);
                if (idx >= 0) {
                    maxIndex = Math.min(maxIndex, idx);
                }
            }
        }

        if (blockInstrs.size() > 0) {
            IrInstr last = blockInstrs.get(blockInstrs.size() - 1);
            if (last instanceof BranchInstr || last instanceof JumpInstr || last instanceof ReturnInstr) {
                if (maxIndex > blockInstrs.size() - 1) {
                    maxIndex = blockInstrs.size() - 1;
                }
            }
        }

        int insertIndex = minIndex;
        // If minIndex > maxIndex, it means we have a conflict or cycle, or topological
        // sort failed.
        // However, since we only move floating instructions and pinned instructions are
        // fixed,
        // and floating instructions are topologically sorted, this should ideally not
        // happen
        // unless there is a dependency on a pinned instruction that appears LATER in
        // the block
        // than a pinned instruction that uses the floating instruction.
        // But pinned instructions order is fixed.
        // If Pinned1 uses Floating, and Floating uses Pinned2.
        // Then Pinned2 must be before Pinned1.
        // If Pinned2 is after Pinned1, then the original code was invalid or GCM broke
        // dependencies.
        // Assuming valid input and GCM preserves dependencies, minIndex <= maxIndex
        // should hold.
        // But to be safe, we can clamp.
        if (insertIndex > maxIndex) {
            insertIndex = maxIndex;
        }

        blockInstrs.add(insertIndex, instr);
        instr.setIrBasicBlock(block);
    }
}