package midend.llvm.value;

import backend.mips.assembly.MipsLabel;
import midend.llvm.IrBuilder;
import midend.llvm.instr.BranchInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.instr.phi.ParallelCopyInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class IrBasicBlock extends IrValue {
    private final IrFunc irFunc;
    private final ArrayList<IrInstr> instrs;

    // CFG
    private final ArrayList<IrBasicBlock> nextBlocks;
    private final ArrayList<IrBasicBlock> beforeBlocks;

    // 支配树
    private final ArrayList<IrBasicBlock> dominatorBlocks; // 支配该结点的结点集合
    private IrBasicBlock immediateDominator; // 直接支配该结点的结点
    private final ArrayList<IrBasicBlock> dominateFrontiers; // 支配边界
    private final ArrayList<IrBasicBlock> immediateDominatedBlocks; // 被该结点直接支配的结点集合

    // 描述活跃变量分析的数据结构
    private HashSet<IrValue> inValueSet = new HashSet<>();
    private HashSet<IrValue> outValueSet = new HashSet<>();
    private HashSet<IrValue> defValueSet = new HashSet<>();
    private HashSet<IrValue> useValueSet = new HashSet<>();

    public IrBasicBlock(String name, IrFunc irFunc) {
        super(IrValueType.BASIC_BLOCK, new IrBaseType(IrBaseType.TypeValue.VOID), name);
        this.irFunc = irFunc;
        instrs = new ArrayList<>();

        nextBlocks = new ArrayList<>();
        beforeBlocks = new ArrayList<>();

        dominatorBlocks = new ArrayList<>();
        immediateDominator = null;
        dominateFrontiers = new ArrayList<>();
        immediateDominatedBlocks = new ArrayList<>();
    }

    public void addInstr(IrInstr instr) {
        instrs.add(instr);
    }

    public void addInstrFirst(IrInstr instr) {
        instrs.add(0, instr);
    }

    public ArrayList<IrInstr> getInstrs() {
        return instrs;
    }

    public IrInstr getLastInstr() {
        return instrs.get(instrs.size() - 1);
    }

    // CFG Methods
    public void addNextBlock(IrBasicBlock block) {
        if (!nextBlocks.contains(block)) {
            nextBlocks.add(block);
        }
    }

    public void addBeforeBlock(IrBasicBlock block) {
        if (!beforeBlocks.contains(block)) {
            beforeBlocks.add(block);
        }
    }

    public void deleteNextBlock(IrBasicBlock block) {
        nextBlocks.remove(block);
        block.beforeBlocks.remove(this);
    }

    public void replaceNextBlock(IrBasicBlock irBasicBlock) {
        this.nextBlocks.remove(irBasicBlock);
        this.nextBlocks.addAll(irBasicBlock.nextBlocks);
    }

    public void replaceBeforeBlock(IrBasicBlock irBasicBlock) {
        this.beforeBlocks.remove(irBasicBlock);
        this.beforeBlocks.addAll(irBasicBlock.beforeBlocks);
    }

    public ArrayList<IrBasicBlock> getNextBlocks() {
        return nextBlocks;
    }

    public ArrayList<IrBasicBlock> getBeforeBlocks() {
        return beforeBlocks;
    }

    public void clearCfg() {
        nextBlocks.clear();
        beforeBlocks.clear();

        dominatorBlocks.clear();
        immediateDominator = null;
        dominateFrontiers.clear();
        immediateDominatedBlocks.clear();
    }

    // Dominator Methods
    public void addDominator(IrBasicBlock block) {
        dominatorBlocks.add(block);
    }

    public ArrayList<IrBasicBlock> getDominatorBlocks() {
        return dominatorBlocks;
    }

    public void setImmediateDominator(IrBasicBlock block) {
        this.immediateDominator = block;
        block.immediateDominatedBlocks.add(this);
    }

    public IrBasicBlock getImmediateDominator() {
        return immediateDominator;
    }

    public void addDominateFrontier(IrBasicBlock block) {
        dominateFrontiers.add(block);
    }

    public ArrayList<IrBasicBlock> getDominateFrontiers() {
        return dominateFrontiers;
    }

    public ArrayList<IrBasicBlock> getImmediateDominatedBlocks() {
        return immediateDominatedBlocks;
    }

    public boolean lastInstrIsReturn() {
        if (instrs.isEmpty()) {
            return false;
        }
        return instrs.get(instrs.size() - 1) instanceof ReturnInstr;
    }

    public IrFunc getIrFunc() {
        return irFunc;
    }

    public void addInstrBeforeJump(IrInstr instr) {
        IrInstr lastInstr = this.getLastInstr();
        if (lastInstr instanceof JumpInstr || lastInstr instanceof BranchInstr) {
            this.instrs.add(this.instrs.size() - 1, instr);
        } else {
            this.instrs.add(instr);
        }
        instr.setIrBasicBlock(this);
    }

    public static IrBasicBlock addMiddleBlock(IrBasicBlock beforeBlock, IrBasicBlock nextBlock) {
        IrBasicBlock middleBlock = IrBuilder.createIrBasicBlock(beforeBlock.getIrFunc(), nextBlock);
        // 修改跳转关系
        if (beforeBlock.getLastInstr() instanceof JumpInstr jumpInstr) {
            jumpInstr.setJumpTarget(middleBlock);
        } else if (beforeBlock.getLastInstr() instanceof BranchInstr branchInstr) {
            if (branchInstr.getTrueBlock() == nextBlock) {
                branchInstr.setTrueBlock(middleBlock);
            } else if (branchInstr.getFalseBlock() == nextBlock) {
                branchInstr.setFalseBlock(middleBlock);
            }
        }
        // 给中间块创建跳转关系
        middleBlock.addInstr(new JumpInstr(nextBlock, middleBlock));

        // 修改原先的流图信息，但是没有重建控制流！
        beforeBlock.nextBlocks.set(beforeBlock.nextBlocks.indexOf(nextBlock), middleBlock);
        nextBlock.beforeBlocks.set(nextBlock.beforeBlocks.indexOf(beforeBlock), middleBlock);
        middleBlock.beforeBlocks.add(beforeBlock);
        middleBlock.nextBlocks.add(nextBlock);

        return middleBlock;
    }

    public boolean isEmpty() {
        return instrs.isEmpty();
    }

    public boolean haveParallelCopyInstr() {
        for (IrInstr instr : instrs) {
            if (instr instanceof ParallelCopyInstr) {
                return true;
            }
        }
        return false;
    }

    public ParallelCopyInstr getAndRemoveParallelCopyInstr() {
        for (int i = 0; i < instrs.size(); i++) {
            if (instrs.get(i) instanceof ParallelCopyInstr) {
                return (ParallelCopyInstr) instrs.remove(i);
            }
        }
        return null;
    }

    public String getFuncName() {
        return irFunc.getName();
    }

    public boolean isEntryBlock() {
        return irFunc.getBasicBlocks().get(0).equals(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":\n\t");
        sb.append(instrs.stream().map(IrInstr::toString).collect(Collectors.joining("\n\t")));
        return sb.toString();
    }

    // Mips
    public void toMips() {
        new MipsLabel(name, MipsLabel.LabelType.BLOCK_NAME);
        for (IrInstr instr : instrs) {
            instr.toMips();
        }
    }

    public void appendBlock(IrBasicBlock nextBlock) {
        // 对于原块的尾跳转
        IrInstr jumpInstr = this.getLastInstr();
        jumpInstr.removeAllUsees();
        this.instrs.remove(jumpInstr);
        // 添加下一个基本快的指令
        nextBlock.instrs.forEach(this::addInstr);
        // 修改next信息
        this.replaceNextBlock(nextBlock);
        // 修改before信息
        for (IrBasicBlock nextNextBlock : nextBlock.nextBlocks) {
            nextNextBlock.replaceBeforeBlock(nextBlock);
            for (IrInstr instr : nextNextBlock.getInstrs()) {
                if (instr instanceof PhiInstr phiInstr) {
                    phiInstr.replaceBlock(nextBlock, this);
                }
            }
        }
    }

    public void replaceLastInstr(IrInstr instr) {
        if (!instrs.isEmpty()) {
            instrs.remove(instrs.size() - 1);
        }
        this.addInstr(instr);
        instr.setIrBasicBlock(this);
    }

    public void clearActiveInfo() {
        inValueSet.clear();
        outValueSet.clear();
        defValueSet.clear();
        useValueSet.clear();
    }

    public HashSet<IrValue> getInValueSet() {
        return inValueSet;
    }

    public void setInValueSet(HashSet<IrValue> inValueSet) {
        this.inValueSet = inValueSet;
    }

    public HashSet<IrValue> getOutValueSet() {
        return outValueSet;
    }

    public void setOutValueSet(HashSet<IrValue> outValueSet) {
        this.outValueSet = outValueSet;
    }

    public HashSet<IrValue> getDefValueSet() {
        return defValueSet;
    }

    public HashSet<IrValue> getUseValueSet() {
        return useValueSet;
    }
}
