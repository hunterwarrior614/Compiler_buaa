package optimize;

import backend.mips.Register;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RegisterAllocator {
    private final ArrayList<Register> registerSet;
    private final HashMap<Register, IrValue> registerValueMap;
    private final HashMap<IrValue, Register> valueRegisterMap;

    public RegisterAllocator(IrFunc irFunction) {
        this.registerValueMap = new HashMap<>();
        this.valueRegisterMap = irFunction.getValueRegisterMap();
        this.registerSet = Register.getUsAbleRegisters();
    }

    public void Allocate(IrBasicBlock entryBlock) {
        // 用于记录信息的数据结构
        // 最后一次使用value是在instr中
        HashMap<IrValue, IrInstr> lastUseMap = new HashMap<>();
        HashSet<IrValue> defineSet = new HashSet<>();
        HashSet<IrValue> neverUseAfterSet = new HashSet<>();

        // 记录当前block的使用信息
        this.RecordLastUse(entryBlock, lastUseMap);
        // 为首块分配寄存器
        this.AllocateOneBlock(entryBlock, lastUseMap, defineSet, neverUseAfterSet);
        // 遍历支配结点
        for (IrBasicBlock childBlock : entryBlock.getImmediateDominatedBlocks()) {
            this.AllocateChildBlock(childBlock);
        }
        // 释放寄存器
        this.FreeDefineValueRegister(defineSet);
        // 递归过程：恢复原先的映射关系：将 后继不再使用但是是从前驱block传过来 的变量对应的寄存器映射恢复回来
        // 也就是 在neverUsedAfter中，但是不是在当前基本块定义的变量
        this.ReCoverRegisterValueMap(defineSet, neverUseAfterSet);
    }

    private void RecordLastUse(IrBasicBlock irBasicBlock, HashMap<IrValue, IrInstr> lastUseMap) {
        for (IrInstr instr : irBasicBlock.getInstrs()) {
            for (IrValue useValue : instr.getUsees()) {
                lastUseMap.put(useValue, instr);
            }
        }
    }

    private void AllocateOneBlock(IrBasicBlock irBasicBlock, HashMap<IrValue, IrInstr> lastUseMap,
            HashSet<IrValue> defineSet, HashSet<IrValue> neverUseAfterSet) {
        for (IrInstr instr : irBasicBlock.getInstrs()) {
            // 检查使用的value是否可以释放寄存器
            this.CheckAndFreeRegister(instr, lastUseMap, neverUseAfterSet);
            // 为当前指令分配寄存器
            this.AllocateInstr(instr, defineSet);
        }
    }

    // 对instr使用的value进行检查
    private void CheckAndFreeRegister(IrInstr instr, HashMap<IrValue, IrInstr> lastUseMap,
            HashSet<IrValue> neverUseAfterSet) {
        // phi不能释放寄存器
        if (instr instanceof PhiInstr) {
            return;
        }

        ArrayList<IrValue> useValueList = instr.getUsees();
        for (IrValue useValue : useValueList) {
            // 1. 有寄存器分配
            // 2. 是最后一次使用
            // 3. 不是out变量
            if (this.valueRegisterMap.containsKey(useValue) &&
                    lastUseMap.get(useValue) == instr &&
                    !instr.getIrBasicBlock().getOutValueSet().contains(useValue)) {

                // 双重检查：是否在后继块的Move指令中使用（ActiveAnalysis可能因CFG问题未覆盖）
                if (isUsedInSuccessorMove(useValue, instr.getIrBasicBlock())) {
                    continue;
                }

                // 之后不参与分配
                this.registerValueMap.remove(this.valueRegisterMap.get(useValue));
                // 后续不会使用
                neverUseAfterSet.add(useValue);
            }
        }
    }

    private boolean isUsedInSuccessorMove(IrValue value, IrBasicBlock block) {
        for (IrBasicBlock nextBlock : block.getNextBlocks()) {
            for (IrInstr instr : nextBlock.getInstrs()) {
                if (instr instanceof MoveInstr moveInstr && moveInstr.getSrcValue() == value) {
                    return true;
                }
            }
        }
        return false;
    }

    private void AllocateInstr(IrInstr instr, HashSet<IrValue> defineSet) {
        if (instr.getIrBaseType().isVoid()) {
            return;
        }

        // 如果已经分配了寄存器，则不再分配
        if (this.valueRegisterMap.containsKey(instr)) {
            return;
        }

        defineSet.add(instr);
        Set<Register> allocatedRegister = this.registerValueMap.keySet();
        for (Register register : this.registerSet) {
            if (!allocatedRegister.contains(register)) {
                this.registerValueMap.put(register, instr);
                this.valueRegisterMap.put(instr, register);
                break;
            }
        }
    }

    private void AllocateChildBlock(IrBasicBlock visitBlock) {
        // 程序运行的逻辑本质上还是线性的：如果子节点和子子结点均用不到某值，则可以释放
        // 使用buffer记录该映射关系，在为子节点分配完成后恢复，以免影响其兄弟节点的寄存器分配
        HashMap<Register, IrValue> bufferMap = new HashMap<>();
        // 子程序不使用即不in
        Set<Register> registerSet = new HashSet<>(this.registerValueMap.keySet());
        for (Register register : registerSet) {
            IrValue registerValue = this.registerValueMap.get(register);
            if (!visitBlock.getInValueSet().contains(registerValue)) {
                bufferMap.put(register, registerValue);
                this.registerValueMap.remove(register);
            }
        }
        // 递归调用
        this.Allocate(visitBlock);
        // 恢复映射关系
        for (Register register : bufferMap.keySet()) {
            this.registerValueMap.put(register, bufferMap.get(register));
        }
    }

    private void FreeDefineValueRegister(HashSet<IrValue> defineSet) {
        for (IrValue value : defineSet) {
            if (this.valueRegisterMap.containsKey(value) && defineSet.contains(value)) {
                this.registerValueMap.remove(this.valueRegisterMap.get(value));
            }
        }
    }

    private void ReCoverRegisterValueMap(HashSet<IrValue> defineSet,
            HashSet<IrValue> neverUseAfterSet) {
        for (IrValue value : neverUseAfterSet) {
            if (this.valueRegisterMap.containsKey(value) && !defineSet.contains(value)) {
                this.registerValueMap.put(this.valueRegisterMap.get(value), value);
            }
        }
    }
}
