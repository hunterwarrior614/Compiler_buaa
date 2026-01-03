package optimize;

import midend.llvm.instr.BranchInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class RemoveUnreachCode extends Optimizer {
    @Override
    public void Optimize() {
        cleanBasicBlocks();
        removeUnreachableBlocks();
        simplifyCFG();
    }

    private void cleanBasicBlocks() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                boolean terminated = false;
                Iterator<IrInstr> it = block.getInstrs().iterator();
                while (it.hasNext()) {
                    IrInstr instr = it.next();
                    if (terminated) {
                        instr.removeAllUsees();
                        it.remove();
                    } else if (isTerminator(instr)) {
                        terminated = true;
                    }
                }
            }
        }
    }

    private boolean isTerminator(IrInstr instr) {
        return instr instanceof JumpInstr || instr instanceof BranchInstr || instr instanceof ReturnInstr;
    }

    private void removeUnreachableBlocks() {
        for (IrFunc func : irModule.getIrFuncs()) {
            if (func.getBasicBlocks().isEmpty())
                continue;

            Set<IrBasicBlock> reachable = new HashSet<>();
            Stack<IrBasicBlock> stack = new Stack<>();

            IrBasicBlock entry = func.getBasicBlocks().get(0);
            stack.push(entry);
            reachable.add(entry);

            while (!stack.isEmpty()) {
                IrBasicBlock block = stack.pop();
                if (block.getInstrs().isEmpty())
                    continue;

                IrInstr last = block.getInstrs().get(block.getInstrs().size() - 1);
                if (last instanceof JumpInstr jump) {
                    addSucc(jump.getJumpBlock(), reachable, stack);
                } else if (last instanceof BranchInstr branch) {
                    addSucc(branch.getTrueBlock(), reachable, stack);
                    addSucc(branch.getFalseBlock(), reachable, stack);
                }
            }

            Iterator<IrBasicBlock> it = func.getBasicBlocks().iterator();
            while (it.hasNext()) {
                IrBasicBlock block = it.next();
                if (!reachable.contains(block)) {
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
                }
            }
        }
    }

    private void addSucc(IrBasicBlock succ, Set<IrBasicBlock> reachable, Stack<IrBasicBlock> stack) {
        if (!reachable.contains(succ)) {
            reachable.add(succ);
            stack.push(succ);
        }
    }

    private void simplifyCFG() {
        boolean changed = true;
        while (changed) {
            changed = false;
            changed |= mergeRedundantBranch();
            changed |= deleteEmptyBlocks();
            changed |= mergeBlocks();
            changed |= hoistBranch();
            if (changed) {
                removeUnreachableBlocks();
            }
        }
    }

    // 1. 合并冗余分支指令
    private boolean mergeRedundantBranch() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                if (block.getInstrs().isEmpty()) continue;
                IrInstr last = block.getLastInstr();
                if (last instanceof BranchInstr branch) {
                    if (branch.getTrueBlock() == branch.getFalseBlock()) {
                        IrBasicBlock target = branch.getTrueBlock();
                        JumpInstr jump = new JumpInstr(target);
                        jump.setIrBasicBlock(block);
                        
                        branch.removeAllUsees();
                        block.getInstrs().set(block.getInstrs().size() - 1, jump);
                        
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    // 2. 删除空程序块
    private boolean deleteEmptyBlocks() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            Iterator<IrBasicBlock> it = func.getBasicBlocks().iterator();
            while (it.hasNext()) {
                IrBasicBlock block = it.next();
                // 只有一条跳转指令
                if (block.getInstrs().size() == 1 && block.getLastInstr() instanceof JumpInstr jump) {
                    IrBasicBlock target = jump.getJumpBlock();
                    if (target == block) continue; // 忽略自环
                    if (func.getBasicBlocks().indexOf(block) == 0) continue; // 入口块不能删

                    ArrayList<IrBasicBlock> preds = new ArrayList<>(block.getBeforeBlocks());
                    if (preds.isEmpty()) continue;

                    // Check for duplicate edges constraint
                    boolean canRemove = true;
                    for (IrBasicBlock pred : preds) {
                        if (target.getBeforeBlocks().contains(pred)) {
                            canRemove = false;
                            break;
                        }
                    }
                    if (!canRemove) continue;

                    // 更新前驱
                    for (IrBasicBlock pred : preds) {
                        IrInstr predLast = pred.getLastInstr();
                        if (predLast instanceof JumpInstr predJump) {
                            predJump.setJumpTarget(target);
                        } else if (predLast instanceof BranchInstr predBranch) {
                            if (predBranch.getTrueBlock() == block) {
                                predBranch.setTrueBlock(target);
                            }
                            if (predBranch.getFalseBlock() == block) {
                                predBranch.setFalseBlock(target);
                            }
                        }
                        
                        // 维护CFG
                        pred.getNextBlocks().remove(block);
                        if (!pred.getNextBlocks().contains(target)) {
                            pred.getNextBlocks().add(target);
                        }
                        if (!target.getBeforeBlocks().contains(pred)) {
                            target.getBeforeBlocks().add(pred);
                        }
                    }

                    // 更新Target的Phi
                    for (IrInstr instr : target.getInstrs()) {
                        if (instr instanceof PhiInstr phi) {
                            // 找到block对应的值
                            int idx = phi.getBeforeBlocks().indexOf(block);
                            if (idx != -1) {
                                IrValue val = phi.getUsees().get(idx);
                                // 移除block
                                phi.removeBlock(block);
                                // 为每个pred添加val
                                for (IrBasicBlock pred : preds) {
                                    phi.addBlock(pred, val);
                                }
                            }
                        }
                    }
                    
                    target.getBeforeBlocks().remove(block);
                    jump.removeAllUsees();
                    it.remove();
                    changed = true;
                    break;
                }
            }
            if (changed) break;
        }
        return changed;
    }

    // 3. 合并程序块
    private boolean mergeBlocks() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            Iterator<IrBasicBlock> it = func.getBasicBlocks().iterator();
            while (it.hasNext()) {
                IrBasicBlock block = it.next();
                if (block.getInstrs().isEmpty()) continue;
                
                if (block.getLastInstr() instanceof JumpInstr jump) {
                    IrBasicBlock target = jump.getJumpBlock();
                    if (target == block) continue;
                    
                    // target只有一个前驱，且就是block
                    if (target.getBeforeBlocks().size() == 1 && target.getBeforeBlocks().get(0) == block) {
                        if (target == func.getBasicBlocks().get(0)) continue; 
                        
                        // 合并 target 到 block
                        // 1. 移除 block 的跳转
                        block.getInstrs().remove(block.getInstrs().size() - 1);
                        jump.removeAllUsees();
                        
                        // 2. 移动 target 的指令到 block
                        for (IrInstr instr : target.getInstrs()) {
                            instr.setIrBasicBlock(block);
                            block.getInstrs().add(instr);
                        }
                        
                        // 3. 更新 target 的后继的前驱指向
                        for (IrBasicBlock succ : target.getNextBlocks()) {
                            succ.replaceBeforeBlock(target); 
                            
                            // Update Phi in succ
                            for (IrInstr instr : succ.getInstrs()) {
                                if (instr instanceof PhiInstr phi) {
                                    phi.replaceBlock(target, block);
                                }
                            }
                        }
                        
                        // 4. 更新 block 的后继
                        block.getNextBlocks().remove(target);
                        for (IrBasicBlock succ : target.getNextBlocks()) {
                            if (!block.getNextBlocks().contains(succ)) {
                                block.getNextBlocks().add(succ);
                            }
                        }
                        
                        // 5. 移除 target
                        func.getBasicBlocks().remove(target);
                        
                        changed = true;
                        break; 
                    }
                }
            }
            if (changed) break;
        }
        return changed;
    }

    // 4. 提升分支指令
    private boolean hoistBranch() {
        boolean changed = false;
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                if (block.getInstrs().isEmpty()) continue;
                if (block.getLastInstr() instanceof JumpInstr jump) {
                    IrBasicBlock target = jump.getJumpBlock();
                    // target 是空块（除了分支指令）
                    if (target.getInstrs().size() == 1 && target.getLastInstr() instanceof BranchInstr branch) {
                        // 提升 branch 到 block
                        
                        // 复制 branch
                        BranchInstr newBranch = new BranchInstr(branch.getCond(), branch.getTrueBlock(), branch.getFalseBlock());
                        
                        // 替换 block 的 jump
                        jump.removeAllUsees();
                        block.getInstrs().set(block.getInstrs().size() - 1, newBranch);
                        newBranch.setIrBasicBlock(block);
                        
                        // 更新 CFG
                        block.getNextBlocks().remove(target);
                        target.getBeforeBlocks().remove(block);
                        
                        if (!block.getNextBlocks().contains(branch.getTrueBlock())) {
                            block.getNextBlocks().add(branch.getTrueBlock());
                        }
                        branch.getTrueBlock().getBeforeBlocks().add(block);
                        
                        if (!block.getNextBlocks().contains(branch.getFalseBlock())) {
                            block.getNextBlocks().add(branch.getFalseBlock());
                        }
                        branch.getFalseBlock().getBeforeBlocks().add(block);
                        
                        updatePhiForHoist(branch.getTrueBlock(), target, block);
                        updatePhiForHoist(branch.getFalseBlock(), target, block);
                        
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
    
    private void updatePhiForHoist(IrBasicBlock succ, IrBasicBlock oldBlock, IrBasicBlock newBlock) {
        for (IrInstr instr : succ.getInstrs()) {
            if (instr instanceof PhiInstr phi) {
                phi.replaceBlock(oldBlock, newBlock);
            }
        }
    }
}
