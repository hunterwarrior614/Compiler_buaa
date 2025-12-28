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
        // 清除之前生成的支配关系
        this.InitFunction();
        // 构建CFG图
        this.BuildCfg();
        // 构建支配关系
        this.BuildDominateRelationship();
        // 构建直接支配关系
        this.BuildDirectDominator();
        // 构建支配边界
        this.BuildDominateFrontier();
    }

    private void InitFunction() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                irBasicBlock.clearCfg();
            }
        }
    }

    private void BuildCfg() {
        // 构建CFG图
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock visitBlock : irFunction.getBasicBlocks()) {
                for (IrInstr instr : visitBlock.getInstrs()) {
                    // 如果是jump
                    if (instr instanceof JumpInstr jumpInstr) {
                        IrBasicBlock targetBlock = jumpInstr.getJumpBlock();
                        visitBlock.addNextBlock(targetBlock);
                        targetBlock.addBeforeBlock(visitBlock);
                    }
                    // 如果是branch
                    else if (instr instanceof BranchInstr branchInstr) {
                        IrBasicBlock trueBlock = branchInstr.getTrueBlock();
                        IrBasicBlock falseBlock = branchInstr.getFalseBlock();
                        visitBlock.addNextBlock(trueBlock);
                        visitBlock.addNextBlock(falseBlock);
                        trueBlock.addBeforeBlock(visitBlock);
                        falseBlock.addBeforeBlock(visitBlock);
                    }
                }
            }
        }
    }

    // 构建支配关系，使用结点删除法：
    // 如果删去图中的某一个结点后，有一些结点变得不可到达，那么这个被删去的结点支配这些变得不可到达的结点
    private void BuildDominateRelationship() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            ArrayList<IrBasicBlock> blockList = irFunction.getBasicBlocks();
            for (IrBasicBlock deleteBlock : blockList) {
                HashSet<IrBasicBlock> visited = new HashSet<>();
                // 从起始点开始遍历，visited存储删除deleteBlock后能访问得到的基本块
                this.SearchDfs(blockList.get(0), deleteBlock, visited);
                // 遍历irFunction中的基本块，如果不在visited中，则说明不可达，deleteBlock支配它
                for (IrBasicBlock visitBlock : blockList) {
                    if (!visited.contains(visitBlock)) {
                        visitBlock.addDominator(deleteBlock);
                    }
                }
            }
        }
    }

    private void SearchDfs(IrBasicBlock visitBlock, IrBasicBlock deleteBlock,
            HashSet<IrBasicBlock> visited) {
        if (visitBlock == deleteBlock) {
            return;
        }

        visited.add(visitBlock);
        for (IrBasicBlock nextBlock : visitBlock.getNextBlocks()) {
            if (!visited.contains(nextBlock) && nextBlock != deleteBlock) {
                this.SearchDfs(nextBlock, deleteBlock, visited);
            }
        }
    }

    // 构建结点的直接支配关系
    private void BuildDirectDominator() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock visitBlock : irFunction.getBasicBlocks()) {
                // 通过删去共同的支配者，来找出最短路径
                for (IrBasicBlock dominator : visitBlock.getDominatorBlocks()) {
                    HashSet<IrBasicBlock> sharedDominators = new HashSet<>(visitBlock.getDominatorBlocks());
                    // 保留共同支配者
                    sharedDominators.retainAll(dominator.getDominatorBlocks());

                    HashSet<IrBasicBlock> diffDominators = new HashSet<>(visitBlock.getDominatorBlocks());
                    // 和支配者不同的支配结点
                    diffDominators.removeAll(sharedDominators);
                    // 支配者的支配着集合中仅有自身，说明直接支配
                    if (diffDominators.size() == 1 && diffDominators.contains(visitBlock)) {
                        visitBlock.setImmediateDominator(dominator);
                        break;
                    }
                }
            }
        }
    }

    private void BuildDominateFrontier() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock visitBlock : irFunction.getBasicBlocks()) {
                ArrayList<IrBasicBlock> nextBlocksBlocks = visitBlock.getNextBlocks();
                for (IrBasicBlock nextBlock : nextBlocksBlocks) {
                    // 指针，沿着直接支配关系进行上溯
                    IrBasicBlock currentBlock = visitBlock;
                    // 后继块就是 cur 或者是 nextBlock 的支配者不包括 cur
                    while (!nextBlock.getDominatorBlocks().contains(currentBlock) || currentBlock == nextBlock) {
                        currentBlock.addDominateFrontier(nextBlock);
                        // 进行上溯
                        currentBlock = currentBlock.getImmediateDominator();
                        if (currentBlock == null) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
