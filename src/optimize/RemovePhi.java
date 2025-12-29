package optimize;


import backend.mips.Register;
import midend.llvm.constant.IrConst;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.instr.phi.ParallelCopyInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RemovePhi extends Optimizer {
    @Override
    public void Optimize() {
        this.ConvertPhiToParallelCopy();
        this.ConvertParallelCopyToMove();
    }

    private void ConvertPhiToParallelCopy() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            ArrayList<IrBasicBlock> blockList = new ArrayList<>(irFunction.getBasicBlocks());
            for (IrBasicBlock irBasicBlock : blockList) {
                if (irBasicBlock.getInstrs().isEmpty() || !(irBasicBlock.getInstrs().get(0) instanceof PhiInstr)) {
                    continue;
                }

                ArrayList<ParallelCopyInstr> copyList = new ArrayList<>();
                for (IrBasicBlock beforeBlock : irBasicBlock.getBeforeBlocks()) {
                    ParallelCopyInstr copyInstr = beforeBlock.getNextBlocks().size() == 1
                            ? this.InsertCopyDirect(beforeBlock)
                            : this.InsertCopyToMiddle(beforeBlock, irBasicBlock);
                    copyList.add(copyInstr);
                }

                Iterator<IrInstr> iterator = irBasicBlock.getInstrs().iterator();
                while (iterator.hasNext()) {
                    IrInstr instr = iterator.next();
                    if (instr instanceof PhiInstr phiInstr) {
                        ArrayList<IrValue> useValueList = phiInstr.getUsees();
                        // 把phi相应的值拷贝到phi指令
                        for (int i = 0; i < useValueList.size(); i++) {
                            IrValue useValue = useValueList.get(i);
                            copyList.get(i).addCopy(useValue, phiInstr);
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    private ParallelCopyInstr InsertCopyDirect(IrBasicBlock beforeBlock) {
        ParallelCopyInstr copyInstr = new ParallelCopyInstr(beforeBlock);
        beforeBlock.addInstrBeforeJump(copyInstr);
        return copyInstr;
    }

    private ParallelCopyInstr InsertCopyToMiddle(IrBasicBlock beforeBlock, IrBasicBlock nextBlock) {
        IrBasicBlock middleBlock = IrBasicBlock.addMiddleBlock(beforeBlock, nextBlock);
        ParallelCopyInstr copyInstr = new ParallelCopyInstr(middleBlock);
        middleBlock.addInstrBeforeJump(copyInstr);
        return copyInstr;
    }

    private void ConvertParallelCopyToMove() {
        for (IrFunc irFunction : irModule.getIrFuncs()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                if (irBasicBlock.haveParallelCopyInstr()) {
                    ParallelCopyInstr copyInstr = irBasicBlock.getAndRemoveParallelCopyInstr();
                    this.ConvertCopyToMove(copyInstr, irBasicBlock);
                }
            }
        }
    }

    private void ConvertCopyToMove(ParallelCopyInstr copyInstr, IrBasicBlock irBasicBlock) {
        // 遍历同时检查冲突现象：后面的move的src为前序的dst
        ArrayList<MoveInstr> moveList = this.ConvertCopy(copyInstr, irBasicBlock);
        // 检查循环赋值冲突
        ArrayList<MoveInstr> circleList =
                this.CheckCircleConflict(copyInstr, irBasicBlock, moveList);
        // 检查寄存器冲突
        ArrayList<MoveInstr> registerList = this.checkRegisterConflict(moveList, irBasicBlock);
        // 在跳转前加入move
        circleList.addAll(registerList);
        moveList.addAll(0, circleList);
        moveList.forEach(irBasicBlock::addInstrBeforeJump);
    }

    private ArrayList<MoveInstr> ConvertCopy(ParallelCopyInstr copyInstr,
                                             IrBasicBlock irBasicBlock) {
        ArrayList<IrValue> srcList = copyInstr.getSrcList();
        ArrayList<IrValue> dstList = copyInstr.getDstList();

        ArrayList<MoveInstr> moveList = new ArrayList<>();
        for (int i = 0; i < dstList.size(); i++) {
            moveList.add(new MoveInstr(srcList.get(i), dstList.get(i), irBasicBlock));
        }

        return moveList;
    }

    private ArrayList<MoveInstr> CheckCircleConflict(
            ParallelCopyInstr copyInstr, IrBasicBlock irBasicBlock, ArrayList<MoveInstr> moveList) {
        ArrayList<IrValue> dstList = copyInstr.getDstList();

        ArrayList<MoveInstr> fixList = new ArrayList<>();
        HashSet<IrValue> valueRecord = new HashSet<>();
        for (int i = 0; i < moveList.size(); i++) {
            IrValue dstValue = dstList.get(i);

            if (!(dstValue instanceof IrConst) && !valueRecord.contains(dstValue)) {
                if (this.HaveCircleConflict(copyInstr, i)) {
                    IrValue middleValue = new IrValue(dstValue.getIrValueType(),
                            dstValue.getIrBaseType(), dstValue.getName() + "_tmp");
                    moveList.add(0, new MoveInstr(dstValue, middleValue, irBasicBlock));
                    // 替换后续指令的src
                    for (MoveInstr moveInstr : moveList) {
                        if (moveInstr.getSrcValue().equals(dstValue)) {
                            moveInstr.setSrcValue(middleValue);
                        }
                    }
                    fixList.add(new MoveInstr(middleValue, dstValue, irBasicBlock));
                }
                valueRecord.add(dstValue);
            }
        }
        return fixList;
    }

    private boolean HaveCircleConflict(ParallelCopyInstr copyInstr, int index) {
        ArrayList<IrValue> srcList = copyInstr.getSrcList();
        ArrayList<IrValue> dstList = copyInstr.getDstList();
        IrValue dstValue = dstList.get(index);
        for (int i = index + 1; i < srcList.size(); i++) {
            if (srcList.get(i).equals(dstValue)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<MoveInstr> checkRegisterConflict(ArrayList<MoveInstr> moveList,
                                                       IrBasicBlock irBasicBlock) {
        ArrayList<MoveInstr> fixList = new ArrayList<>();
        HashSet<IrValue> valueRecord = new HashSet<>();
        for (int i = moveList.size() - 1; i >= 0; i--) {
            IrValue srcValue = moveList.get(i).getSrcValue();
            if (!(srcValue instanceof IrConst) && !valueRecord.contains(srcValue)) {
                if (this.HaveRegisterConflict(moveList, i, irBasicBlock)) {
                    IrValue middleValue = new IrValue(srcValue.getIrValueType(), srcValue.getIrBaseType(),
                            srcValue.getName() + "_tmp");
                    // 将所有相同指令的src替换为临时
                    for (MoveInstr moveInstr : moveList) {
                        if (moveInstr.getSrcValue() == srcValue) {
                            moveInstr.setSrcValue(middleValue);
                        }
                    }
                    // 在moveList开头加入新move
                    MoveInstr moveInstr = new MoveInstr(srcValue, middleValue, irBasicBlock);
                    fixList.add(moveInstr);
                }
                valueRecord.add(srcValue);
            }
        }
        return fixList;
    }

    private boolean HaveRegisterConflict(ArrayList<MoveInstr> moveList, int index,
                                         IrBasicBlock irBasicBlock) {
        HashMap<IrValue, Register> registerMap = irBasicBlock.getIrFunc().getValueRegisterMap();
        IrValue srcValue = moveList.get(index).getSrcValue();
        Register srcRegister = registerMap.get(srcValue);

        if (srcRegister != null) {
            for (int i = 0; i < index; i++) {
                IrValue dstValue = moveList.get(i).getDstValue();
                if (registerMap.get(dstValue).equals(srcRegister)) {
                    return true;
                }
            }
        }
        return false;
    }
}
