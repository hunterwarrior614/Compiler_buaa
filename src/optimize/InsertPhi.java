package optimize;

import midend.llvm.constant.IrConstInt;
import midend.llvm.instr.AllocateInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.LoadInstr;
import midend.llvm.instr.StoreInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.use.IrUse;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class InsertPhi {
    private final AllocateInstr allocateInstr;
    private final IrBasicBlock entryBlock;
    private final HashSet<IrInstr> defineInstrs;
    private final HashSet<IrInstr> useInstrs;
    private final ArrayList<IrBasicBlock> defineBlocks;
    private final ArrayList<IrBasicBlock> useBlocks;
    private Stack<IrValue> valueStack;

    public InsertPhi(AllocateInstr allocateInstr, IrBasicBlock entryBlock) {
        this.allocateInstr = allocateInstr;
        this.entryBlock = entryBlock;
        this.defineInstrs = new HashSet<>();
        this.useInstrs = new HashSet<>();
        this.defineBlocks = new ArrayList<>();
        this.useBlocks = new ArrayList<>();
        this.valueStack = new Stack<>();
    }

    public void addPhi() {
        // 分析该allocateInstr的define和use关系
        buildDefineUseRelationship();
        // 找出需要添加phi指令的基本块，并添加phi
        insertPhiToBlock();
        // 通过DFS进行重命名，同时将相关的allocate, store,load指令删除
        convertLoadStore(entryBlock);
    }

    private void buildDefineUseRelationship() {
        // 所有使用该allocate的user
        for (IrUse irUse : allocateInstr.getUseList()) {
            IrInstr userInstr = (IrInstr) irUse.getUser();
            // load关系为use关系
            if (userInstr instanceof LoadInstr) {
                this.addUseInstr(userInstr);
            }
            // store关系为define关系
            else if (userInstr instanceof StoreInstr storeInstr) {
                if (storeInstr.getAddress() == this.allocateInstr) {
                    this.addDefineInstr(userInstr);
                }
            }
        }
    }

    private void addDefineInstr(IrInstr instr) {
        defineInstrs.add(instr);
        if (!defineBlocks.contains(instr.getIrBasicBlock())) {
            defineBlocks.add(instr.getIrBasicBlock());
        }
    }

    private void addUseInstr(IrInstr instr) {
        useInstrs.add(instr);
        if (!useBlocks.contains(instr.getIrBasicBlock())) {
            useBlocks.add(instr.getIrBasicBlock());
        }
    }

    private void insertPhiToBlock() {
        // 需要添加phi的基本块的集合
        HashSet<IrBasicBlock> addedPhiBlocks = new HashSet<>();

        // 定义变量的基本块的集合
        Stack<IrBasicBlock> defineBlockStack = new Stack<>();
        for (IrBasicBlock defineBlock : defineBlocks) {
            defineBlockStack.push(defineBlock);
        }

        while (!defineBlockStack.isEmpty()) {
            IrBasicBlock defineBlock = defineBlockStack.pop();
            // 遍历当前基本块的所有后继基本块
            for (IrBasicBlock frontierBlock : defineBlock.getDominateFrontiers()) {
                // 如果后继基本块不在F中，则将后继基本块加入F中
                if (!addedPhiBlocks.contains(frontierBlock)) {
                    insertPhiInstr(frontierBlock);
                    addedPhiBlocks.add(frontierBlock);
                    // phi也进行定义变量
                    if (!defineBlocks.contains(frontierBlock)) {
                        defineBlockStack.push(frontierBlock);
                    }
                }
            }
        }
    }

    private void insertPhiInstr(IrBasicBlock irBasicBlock) {
        PhiInstr phiInstr = new PhiInstr(allocateInstr.getIrBaseType().getPointValueType(), irBasicBlock);
        irBasicBlock.addInstrFirst(phiInstr);
        // phi既是define，又是use
        useInstrs.add(phiInstr);
        defineInstrs.add(phiInstr);
    }

    private void convertLoadStore(IrBasicBlock renameBlock) {
        final Stack<IrValue> stackCopy = (Stack<IrValue>) valueStack.clone();
        // 移除与当前allocate相关的全部的load、store指令
        removeBlockLoadStore(renameBlock);
        // 遍历entry的后续集合，将最新的define填充进每个后继块的第一个phi指令中
        convertPhiValue(renameBlock);
        // 对支配块进行dfs
        for (IrBasicBlock dominateBlock : renameBlock.getImmediateDominatedBlocks()) {
            convertLoadStore(dominateBlock);
        }
        // 恢复栈
        valueStack = stackCopy;
    }

    private void removeBlockLoadStore(IrBasicBlock visitBlock) {
        Iterator<IrInstr> iterator = visitBlock.getInstrs().iterator();
        while (iterator.hasNext()) {
            IrInstr instr = iterator.next();
            // store
            if (instr instanceof StoreInstr storeInstr && defineInstrs.contains(instr)) {
                valueStack.push(storeInstr.getValue());
                iterator.remove();
            }
            // load
            else if (!(instr instanceof PhiInstr) && useInstrs.contains(instr)) {
                instr.replaceAllUsesWith(peekValueStack());
                iterator.remove();
            }
            // phi
            else if (instr instanceof PhiInstr && defineInstrs.contains(instr)) {
                valueStack.push(instr);
            }
            // 当前分析的allocate：使用mem2reg后不需要allocate
            else if (instr == allocateInstr) {
                iterator.remove();
            }
        }
    }

    private void convertPhiValue(IrBasicBlock visitBlock) {
        for (IrBasicBlock nextBlock : visitBlock.getNextBlocks()) {
            if (nextBlock.getInstrs().isEmpty())
                continue;
            IrInstr firstInstr = nextBlock.getInstrs().get(0);
            if (firstInstr instanceof PhiInstr phiInstr && useInstrs.contains(firstInstr)) {
                phiInstr.ConvertBlockToValue(peekValueStack(), visitBlock);
            }
        }
    }

    private IrValue peekValueStack() {
        return valueStack.isEmpty() ? new IrConstInt(0) : valueStack.peek();
    }
}
