package backend.mips;

import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.data.MipsDataAssembly;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrParameter;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MipsBuilder {
    private static MipsModule mipsModule = null;
    private static HashMap<IrValue, Register> currentValueRegisterMap = null;    // 当前函数的value-register分配表

    private static int stackOffset = 0;
    private static HashMap<IrValue, Integer> stackOffsetValueMap = null;

    public static void setMipsModule(MipsModule mipsModule) {
        MipsBuilder.mipsModule = mipsModule;
    }

    public static void addMipsAssembly(MipsAssembly mipsAssembly) {
        if (mipsAssembly instanceof MipsDataAssembly) {
            mipsModule.addToDataSeg(mipsAssembly);
        } else {
            mipsModule.addToTextSeg(mipsAssembly);
        }
    }

    public static void setCurrentFunction(IrFunc irFunc) {
        currentValueRegisterMap = irFunc.getValueRegisterMap();

        stackOffset = 0;
        stackOffsetValueMap = new HashMap<>();
    }

    public static void mapIrParameter2Register(IrParameter irParameter, Register register) {
        currentValueRegisterMap.put(irParameter, register);
    }

    // 获取到IrValue对应的寄存器
    public static Register getRegisterOfIrValue(IrValue irValue) {
        return currentValueRegisterMap.get(irValue);
    }

    public static ArrayList<Register> getCurrentAllocatedRegisters() {
        return new ArrayList<>(new HashSet<>(currentValueRegisterMap.values()));
    }

    // 获取到IrValue对应的栈偏移
    // 这里不能用 int 因为可能 key 没有irValue
    public static Integer getStackOffsetOfIrValue(IrValue irValue) {
        return stackOffsetValueMap.get(irValue);
    }

    // 为IrValue申请一块栈空间（不涉及 MIPS 操作）
    public static Integer allocateStackSpaceForIrValue(IrValue irValue) {
        // TODO:why
        Integer address = stackOffsetValueMap.get(irValue);
        if (address == null) {
            stackOffset -= 4;
            stackOffsetValueMap.put(irValue, stackOffset);
            address = stackOffset;
        }
        return address;
    }

    // 申请一块指定大小的栈空间（不涉及 MIPS 操作）
    public static void allocateStackSpace(int offset) {
        stackOffset -= offset;
    }

    public static int getCurrentStackOffset() {
        return stackOffset;
    }

}
