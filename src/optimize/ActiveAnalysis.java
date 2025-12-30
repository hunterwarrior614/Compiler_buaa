package optimize;

import midend.MidEnd;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrGlobalVariable;
import midend.llvm.value.IrParameter;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashSet;

public class ActiveAnalysis extends Optimizer {
    @Override
    public void Optimize() {
        // 清除原先的活跃分析
        this.ClearActiveInfo();
        // 分析def和use
        this.AnalysisDefAndUse();
        // 分析in和out
        this.AnalysisInAndOut();
    }

    private void ClearActiveInfo() {
        for (IrFunc irFunction : MidEnd.getIrModule().getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                irBasicBlock.clearActiveInfo();
            }
        }
    }

    private void AnalysisDefAndUse() {
        for (IrFunc irFunction : MidEnd.getIrModule().getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                HashSet<IrValue> defSet = irBasicBlock.getDefValueSet();
                HashSet<IrValue> useSet = irBasicBlock.getUseValueSet();

                // 先分析phi指令：由前序时间传入，最早发生
                for (IrInstr instr : irBasicBlock.getInstrs()) {
                    if (instr instanceof PhiInstr phiInstr) {
                        for (IrValue useValue : phiInstr.getUsees()) {
                            if (this.IsUseValue(useValue)) {
                                useSet.add(useValue);
                            }
                        }
                    }
                }
                // 分析其他指令
                for (IrInstr instr : irBasicBlock.getInstrs()) {
                    // 对instr使用的数据分析
                    for (IrValue useValue : instr.getUsees()) {
                        if (instr instanceof MoveInstr moveInstr && useValue == moveInstr.getDstValue()) {
                            continue;
                        }
                        if (!defSet.contains(useValue) && this.IsUseValue(useValue)) {
                            useSet.add(useValue);
                        }
                    }
                    // 对instr本身
                    // 注意：IrInstr 如果返回类型是 VOID，则不应该被视为定义了值
                    // 但是在 LLVM IR 中，即使是 void 类型的指令也可能是一个 IrValue (虽然不能被使用)
                    // 这里我们需要判断 instr 是否定义了一个可以被使用的值
                    // 通常，如果 instr 的类型不是 VOID，它就定义了一个值
                    if (instr instanceof MoveInstr moveInstr) {
                        defSet.add(moveInstr.getDstValue());
                    } else if ((!useSet.contains(instr) || !instr.getIrBaseType().isVoid())
                            && !instr.getIrBaseType().isVoid()) {
                        defSet.add(instr);
                    }
                }
            }
        }
    }

    private boolean IsUseValue(IrValue useValue) {
        return useValue instanceof IrInstr ||
                useValue instanceof IrParameter ||
                useValue instanceof IrGlobalVariable;
    }

    // in[B] = use[B] \cup (out[B] - def[B])
    // out[B] = \cup in[P] P为B的后继基本块
    private void AnalysisInAndOut() {
        for (IrFunc irFunction : MidEnd.getIrModule().getIrFuncs()) {
            ArrayList<IrBasicBlock> blockList = irFunction.getBasicBlocks();
            // 进行分析，直到不发生改变
            boolean haveChange = true;
            while (haveChange) {
                haveChange = false;
                // 对block进行逆序分析

                for (int i = blockList.size() - 1; i >= 0; i--) {
                    IrBasicBlock analysisBlock = blockList.get(i);

                    // out：对于后继块
                    HashSet<IrValue> newOutValueSet = new HashSet<>();
                    for (IrBasicBlock nextBlock : analysisBlock.getNextBlocks()) {
                        newOutValueSet.addAll(nextBlock.getInValueSet());
                    }
                    // in：
                    HashSet<IrValue> newInValueSet = new HashSet<>(newOutValueSet);
                    newInValueSet.removeAll(analysisBlock.getDefValueSet());
                    newInValueSet.addAll(analysisBlock.getUseValueSet());

                    HashSet<IrValue> oldInValueSet = analysisBlock.getInValueSet();
                    HashSet<IrValue> oldOutValueSet = analysisBlock.getOutValueSet();
                    if (!newOutValueSet.equals(oldOutValueSet) ||
                            !newInValueSet.equals(oldInValueSet)) {
                        haveChange = true;
                        analysisBlock.setInValueSet(newInValueSet);
                        analysisBlock.setOutValueSet(newOutValueSet);
                    }
                }
            }
        }
    }
}
