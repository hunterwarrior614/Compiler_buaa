package optimize;

import midend.llvm.instr.BranchInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class RemoveUnreachCode extends Optimizer {
    @Override
    public void Optimize() {
        cleanBasicBlocks();
        removeUnreachableBlocks();
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
}