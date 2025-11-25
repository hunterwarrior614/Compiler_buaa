package midend.llvm;


import midend.llvm.constant.IrConst;
import midend.llvm.constant.IrConstStr;
import midend.llvm.instr.IrInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrGlobalVariable;
import midend.llvm.value.IrLoop;

import java.util.HashMap;
import java.util.Stack;

public class IrBuilder {
    private static IrModule irModule = null;
    private static IrFunc currentIrFunc = null;
    private static IrBasicBlock currentIrBasicBlock = null;
    private static final HashMap<String, Integer> localVarCountMap = new HashMap<>();   // 用于计数函数中出现的变量（虚拟寄存器）
    private static final Stack<IrLoop> loopStack = new Stack<>();   // 用于存储嵌套循环

    private static int basicBlockCount = 0;
    private static int constStrCount = 0;

    public static void setIrModule(IrModule irModule) {
        IrBuilder.irModule = irModule;
    }

    public static IrFunc createIrFunc(IrBaseType returnType, String name) {
        // 创建新IrFunction
        String funcName = "@" + name;
        IrFunc irFunc = new IrFunc(funcName, returnType);
        irModule.addIrFunc(irFunc);
        // 设置当前处理的IrFunction
        currentIrFunc = irFunc;
        // 为IrFunction添加一个基本块并设置当前的基本块
        currentIrBasicBlock = createIrBasicBlock();

        // 加入计数表中
        localVarCountMap.put(funcName, -1);

        return irFunc;
    }

    public static IrBasicBlock createIrBasicBlock() {
        IrBasicBlock irBasicBlock = new IrBasicBlock("b_" + basicBlockCount++, currentIrFunc);
        currentIrFunc.addBasicBlock(irBasicBlock);

        return irBasicBlock;
    }

    public static void setCurrentIrBasicBlock(IrBasicBlock irBasicBlock) {
        currentIrBasicBlock = irBasicBlock;
    }

    public static IrBasicBlock getCurrentIrBasicBlock() {
        return currentIrBasicBlock;
    }

    public static void addInstr(IrInstr irInstr) {
        currentIrBasicBlock.addInstr(irInstr);
        irInstr.setIrBasicBlock(currentIrBasicBlock);
    }

    public static IrGlobalVariable createIrGlobalVariable(String name, IrConst initValue) {
        IrGlobalVariable irGlobalVariable = new IrGlobalVariable(name,
                new IrBaseType(IrBaseType.TypeValue.POINTER, initValue.getIrBaseType()),
                initValue);
        irModule.addGlobalVariable(irGlobalVariable);

        return irGlobalVariable;
    }

    // 虚拟寄存器
    public static String getLocalVarName() {
        int count = localVarCountMap.get(currentIrFunc.getName());
        count++;
        localVarCountMap.put(currentIrFunc.getName(), count);
        return "%var_" + count;
    }

    public static IrConstStr createIrConstStr(String constStr) {
        IrConstStr irConstStr = irModule.getConstStr(constStr);
        if (irConstStr == null) {
            irConstStr = new IrConstStr(getStrName(), constStr);
            irModule.addConstStr(irConstStr);
        }
        return irConstStr;
    }

    public static String getStrName() {
        return "@s_" + constStrCount++;
    }

    public static void pushLoop(IrLoop loop) {
        loopStack.push(loop);
    }

    public static void popLoop() {
        loopStack.pop();
    }

    public static IrLoop getCurrentLoop() {
        return loopStack.peek();
    }
}
