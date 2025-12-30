package optimize;

import midend.llvm.instr.*;
import midend.llvm.instr.io.IOInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.*;

public class RemoveDeadCode extends Optimizer {
    // 记录调用了哪些函数
    private final HashMap<IrFunc, HashSet<IrFunc>> calleeMap;
    // 记录被哪些函数调用
    private final HashMap<IrFunc, HashSet<IrFunc>> callerMap;
    // 副作用：有IO操作
    private final HashSet<IrFunc> sideEffectFunctions;

    public RemoveDeadCode() {
        this.calleeMap = new HashMap<>();
        this.callerMap = new HashMap<>();
        this.sideEffectFunctions = new HashSet<>();
    }

    @Override
    public void Optimize() {
        boolean finished = false;
        while (!finished) {
            this.BuildFunctionCallMap();
            finished = this.RemoveUselessFunction();
            finished &= this.RemoveUselessBlock();
            finished &= this.RemoveUselessCode();
            finished &= this.RemoveUselessPhi();
            finished &= this.MergeBlock();
        }
    }

    private void BuildFunctionCallMap() {
        // 进行初始化
        this.calleeMap.clear();
        this.callerMap.clear();
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            this.calleeMap.put(irFunction, new HashSet<>());
            this.callerMap.put(irFunction, new HashSet<>());
        }
        // 进行dfs
        this.DfsSideFunction(irModule.getMainFunction(), new HashSet<>());
    }

    private void DfsSideFunction(IrFunc visitFunction, HashSet<IrFunc> visited) {
        if (visited.contains(visitFunction)) {
            return;
        }
        visited.add(visitFunction);

        for (IrBasicBlock irBasicBlock : visitFunction.getBasicBlocks()) {
            for (IrInstr instr : irBasicBlock.getInstrs()) {
                // 函数调用
                if (instr instanceof CallInstr callInstr) {
                    IrFunc callee = callInstr.getFunc();
                    this.DfsSideFunction(callee, visited);

                    this.calleeMap.get(visitFunction).add(callee);
                    this.callerMap.get(callee).add(visitFunction);
                    // 对与函数副作用
                    if (this.sideEffectFunctions.contains(callee)) {
                        this.sideEffectFunctions.add(visitFunction);
                    }
                }
                // IO、存操作
                else if (instr instanceof IOInstr || instr instanceof StoreInstr) {
                    this.sideEffectFunctions.add(visitFunction);
                }
            }
        }
    }

    // 删除无用函数
    private boolean RemoveUselessFunction() {
        boolean finished = true;

        Iterator<IrFunc> iterator = irModule.getIrFuncs().iterator();
        while (iterator.hasNext()) {
            IrFunc irFunction = iterator.next();
            // 无人调用，删除：即使有sideEffect也没关系
            if (!irFunction.isMainFunction() && this.callerMap.get(irFunction).isEmpty()) {
                iterator.remove();
                finished = false;
            }
        }

        return finished;
    }

    // 删除无用基本块
    private boolean RemoveUselessBlock() {
        boolean finished = true;
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            Iterator<IrBasicBlock> iterator = irFunction.getBasicBlocks().iterator();
            while (iterator.hasNext()) {
                IrBasicBlock visitBlock = iterator.next();
                // 不可达块，删除
                if (visitBlock.getBeforeBlocks().isEmpty() && !visitBlock.isEntryBlock()) {
                    // 改变关系
                    for (IrBasicBlock nextBlock : visitBlock.getNextBlocks()) {
                        nextBlock.getBeforeBlocks().remove(visitBlock);
                        // 消除phi
                        for (IrInstr nextInstr : nextBlock.getInstrs()) {
                            if (nextInstr instanceof PhiInstr phiInstr) {
                                phiInstr.removeBlock(visitBlock);
                            }
                        }
                    }
                    // 删除指令
                    for (IrInstr instr : visitBlock.getInstrs()) {
                        instr.removeAllUsees();
                    }

                    finished = false;
                    iterator.remove();
                }
            }
        }
        return finished;
    }

    // 删除无用代码
    private boolean RemoveUselessCode() {
        boolean finished = true;
        HashSet<IrInstr> activeInstrSet = this.GetActiveInstrSet();

        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                Iterator<IrInstr> iterator = irBasicBlock.getInstrs().iterator();
                while (iterator.hasNext()) {
                    IrInstr instr = iterator.next();
                    if (!activeInstrSet.contains(instr)) {
                        instr.removeAllUsees();
                        iterator.remove();
                        finished = false;
                    }
                }
            }
        }

        return finished;
    }

    private HashSet<IrInstr> GetActiveInstrSet() {
        HashSet<IrInstr> activeInstrSet = new HashSet<>();
        Stack<IrInstr> todoInstrStack = new Stack<>();
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                for (IrInstr instr : irBasicBlock.getInstrs()) {
                    if (this.IsCriticalInstr(instr)) {
                        todoInstrStack.push(instr);
                    }
                }
            }
        }

        while (!todoInstrStack.isEmpty()) {
            IrInstr todoInstr = todoInstrStack.pop();
            activeInstrSet.add(todoInstr);
            for (IrValue useValue : todoInstr.getUsees()) {
                if (useValue instanceof IrInstr useInstr) {
                    if (!activeInstrSet.contains(useInstr)) {
                        todoInstrStack.push(useInstr);
                    }
                    activeInstrSet.add(useInstr);
                }
            }
        }

        return activeInstrSet;
    }

    private boolean IsCriticalInstr(IrInstr instr) {
        return instr instanceof ReturnInstr ||
                (instr instanceof CallInstr callInstr &&
                        this.sideEffectFunctions.contains(callInstr.getFunc())) ||
                instr instanceof BranchInstr || instr instanceof JumpInstr ||
                instr instanceof StoreInstr || instr instanceof IOInstr;
    }

    private boolean RemoveUselessPhi() {
        boolean finished = true;
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                Iterator<IrInstr> iterator = irBasicBlock.getInstrs().iterator();
                while (iterator.hasNext()) {
                    IrInstr instr = iterator.next();
                    if (!(instr instanceof PhiInstr phiInstr)) {
                        continue;
                    }

                    ArrayList<IrValue> phiValueList = phiInstr.getUsees();
                    if (phiValueList.size() == 1) {
                        finished = false;
                        phiInstr.replaceAllUsesWith(phiValueList.get(0));
                        phiInstr.removeAllUsees();
                        iterator.remove();
                    }
                }
            }
        }

        return finished;
    }

    private boolean MergeBlock() {
        boolean finished = true;

        for (IrFunc irFunction : irModule.getIrFuncs()) {
            Iterator<IrBasicBlock> iterator = irFunction.getBasicBlocks().iterator();
            while (iterator.hasNext()) {
                IrBasicBlock irBasicBlock = iterator.next();
                if (this.CanMergeBlock(irBasicBlock)) {
                    finished = false;
                    IrBasicBlock beforeBlock = irBasicBlock.getBeforeBlocks().get(0);
                    beforeBlock.appendBlock(irBasicBlock);
                    iterator.remove();
                }
            }
        }

        return finished;
    }

    private boolean CanMergeBlock(IrBasicBlock visitBlock) {
        ArrayList<IrBasicBlock> beforeBlockList = visitBlock.getBeforeBlocks();
        if (beforeBlockList.size() == 1) {
            IrBasicBlock beforeBlock = beforeBlockList.get(0);
            // 前后对接上，则可以合并
            return beforeBlock.getNextBlocks().size() == 1 &&
                    beforeBlock.getNextBlocks().get(0) == visitBlock;
        }
        return false;
    }
}
