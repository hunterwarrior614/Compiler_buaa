package optimize;

import midend.llvm.instr.BranchInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;

import java.util.HashSet;
import java.util.Iterator;

public class RemoveUnreachCode extends Optimizer {
    @Override
    public void Optimize() {
        // 删除多余的jump
        RemoveUselessJump();
        // 删除不可达块
        RemoveUselessBlock();
    }

    private void RemoveUselessJump() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                boolean hasJump = false;
                Iterator<IrInstr> iterator = irBasicBlock.getInstrs().iterator();
                while (iterator.hasNext()) {
                    IrInstr instr = iterator.next();
                    if (hasJump) {
                        instr.removeAllUsees();
                        iterator.remove();
                        continue;
                    }

                    if (instr instanceof JumpInstr || instr instanceof BranchInstr ||
                        instr instanceof ReturnInstr) {
                        hasJump = true;
                    }
                }
            }
        }
    }

    private void RemoveUselessBlock() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            IrBasicBlock entryBlock = irFunction.getBasicBlocks().get(0);
            HashSet<IrBasicBlock> visited = new HashSet<>();
            // 使用dfs记录可达的block
            DfsBlock(entryBlock, visited);
            irFunction.getBasicBlocks().removeIf(block -> !visited.contains(block));
        }
    }

    private void DfsBlock(IrBasicBlock block, HashSet<IrBasicBlock> visited) {
        if (visited.contains(block)) {
            return;
        }

        visited.add(block);
        // 一定是跳转
        IrInstr instr = block.getLastInstr();
        // return
        if (instr instanceof ReturnInstr) {
            return;
        }
        // jump
        else if (instr instanceof JumpInstr jumpInstr) {
            IrBasicBlock targetBlock = jumpInstr.getJumpBlock();
            DfsBlock(targetBlock, visited);
        }
        // branch
        else if (instr instanceof BranchInstr branchInstr) {
            IrBasicBlock trueBlock = branchInstr.getTrueBlock();
            IrBasicBlock falseBlock = branchInstr.getFalseBlock();
            DfsBlock(trueBlock, visited);
            DfsBlock(falseBlock, visited);
        }
    }
}