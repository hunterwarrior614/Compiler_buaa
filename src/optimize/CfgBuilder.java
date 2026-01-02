package optimize;

import midend.llvm.instr.BranchInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.JumpInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;

import java.util.ArrayList;
import java.util.HashSet;

// 构建CFG支配图
public class CfgBuilder extends Optimizer {
    @Override
    public void Optimize() {
        this.clearCfgInfo();
        this.constructFlowGraph();
        this.calculateDominators();
        this.calculateImmediateDominators();
        this.calculateDominanceFrontiers();
    }

    private void clearCfgInfo() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                block.clearCfg();
            }
        }
    }

    private void constructFlowGraph() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                if (block.getInstrs().isEmpty())
                    continue;
                IrInstr lastInstr = block.getInstrs().get(block.getInstrs().size() - 1);

                if (lastInstr instanceof JumpInstr jump) {
                    IrBasicBlock target = jump.getJumpBlock();
                    this.linkBlocks(block, target);
                } else if (lastInstr instanceof BranchInstr branch) {
                    this.linkBlocks(block, branch.getTrueBlock());
                    this.linkBlocks(block, branch.getFalseBlock());
                }
            }
        }
    }

    private void linkBlocks(IrBasicBlock pred, IrBasicBlock succ) {
        pred.addNextBlock(succ);
        succ.addBeforeBlock(pred);
    }

    // 使用迭代算法构建支配关系
    private void calculateDominators() {
        for (IrFunc func : irModule.getIrFuncs()) {
            ArrayList<IrBasicBlock> blocks = func.getBasicBlocks();
            if (blocks.isEmpty())
                continue;

            IrBasicBlock entry = blocks.get(0);

            // 初始化：Dom(entry) = {entry}, Dom(others) = {all blocks}
            for (IrBasicBlock block : blocks) {
                if (block == entry) {
                    block.addDominator(entry);
                } else {
                    for (IrBasicBlock b : blocks) {
                        block.addDominator(b);
                    }
                }
            }

            boolean changed = true;
            while (changed) {
                changed = false;
                for (IrBasicBlock block : blocks) {
                    if (block == entry)
                        continue;

                    // NewDom = {block} U (Intersection of Dom(p) for all p in preds)
                    ArrayList<IrBasicBlock> preds = block.getBeforeBlocks();

                    if (preds.isEmpty()) {
                        continue;
                    }

                    // 初始化交集为第一个前驱的支配集合
                    HashSet<IrBasicBlock> newDom = new HashSet<>(preds.get(0).getDominatorBlocks());

                    for (int i = 1; i < preds.size(); i++) {
                        newDom.retainAll(preds.get(i).getDominatorBlocks());
                    }

                    newDom.add(block);

                    // 检查是否发生变化
                    ArrayList<IrBasicBlock> currentDom = block.getDominatorBlocks();
                    // 注意：这里比较集合内容是否一致
                    if (newDom.size() != currentDom.size() || !newDom.containsAll(currentDom)) {
                        currentDom.clear();
                        currentDom.addAll(newDom);
                        changed = true;
                    }
                }
            }
        }
    }

    // 构建直接支配关系
    private void calculateImmediateDominators() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                if (block == func.getBasicBlocks().get(0))
                    continue;

                ArrayList<IrBasicBlock> dominators = block.getDominatorBlocks();
                // IDom(n) 是 Dom(n)-{n} 中支配集合大小最大的那个结点
                IrBasicBlock idom = null;
                int maxDoms = -1;

                for (IrBasicBlock dom : dominators) {
                    if (dom == block)
                        continue;

                    int size = dom.getDominatorBlocks().size();
                    if (size > maxDoms) {
                        maxDoms = size;
                        idom = dom;
                    }
                }

                if (idom != null) {
                    block.setImmediateDominator(idom);
                }
            }
        }
    }

    // 构建支配边界
    private void calculateDominanceFrontiers() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                ArrayList<IrBasicBlock> successors = block.getNextBlocks();

                for (IrBasicBlock succ : successors) {
                    IrBasicBlock runner = block;
                    // 只要 runner 不是 succ 的直接支配者，就继续上溯
                    while (runner != succ.getImmediateDominator() && runner != null) {
                        runner.addDominateFrontier(succ);
                        runner = runner.getImmediateDominator();
                    }
                }
            }
        }
    }
}
