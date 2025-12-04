package midend.llvm;

import backend.mips.Register;
import backend.mips.assembly.MipsAnnotation;
import backend.mips.assembly.MipsLabel;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.text.MipsJump;
import backend.mips.assembly.text.MipsSyscall;
import midend.llvm.constant.IrConstStr;
import midend.llvm.instr.io.GetIntInstr;
import midend.llvm.instr.io.PrintIntInstr;
import midend.llvm.instr.io.PrintStrInstr;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrGlobalVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class IrModule {
    private final ArrayList<String> declares;   // 用于库函数声明
    private final HashMap<String, IrConstStr> constStrMap;  // 程序中出现的字符串
    private final ArrayList<IrGlobalVariable> globalVariables; // 全局变量声明
    private final ArrayList<IrFunc> functions;  // 函数定义

    public IrModule() {
        this.declares = new ArrayList<>();
        this.constStrMap = new HashMap<>();
        globalVariables = new ArrayList<>();
        this.functions = new ArrayList<>();

        this.declares.add(GetIntInstr.getDeclare());
        this.declares.add(PrintIntInstr.getDeclare());
        this.declares.add(PrintStrInstr.getDeclare());
    }

    public void addIrFunc(IrFunc irFunc) {
        functions.add(irFunc);
    }

    public void addGlobalVariable(IrGlobalVariable irGlobalVariable) {
        globalVariables.add(irGlobalVariable);
    }


    public IrConstStr getConstStr(String constStr) {
        if (constStrMap.containsKey(constStr)) {
            return constStrMap.get(constStr);
        }
        return null;
    }

    public void addConstStr(IrConstStr irConstStr) {
        constStrMap.put(irConstStr.getName(), irConstStr);
    }

    public void checkEmptyBasicBlocks() {
        for (IrFunc func : functions) {
            func.checkEmptyBasicBlocks();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\n", declares)).append(declares.isEmpty() ? "" : "\n\n");
        sb.append(globalVariables.stream().map(IrGlobalVariable::toString).collect(Collectors.joining("\n"))).append(globalVariables.isEmpty() ? "" : "\n\n");
        sb.append(constStrMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())  // 按key排序
                        .map(entry -> entry.getValue().toString())
                        .collect(Collectors.joining("\n")))
                .append(constStrMap.isEmpty() ? "" : "\n\n");
        sb.append(functions.stream().map(IrFunc::toString).collect(Collectors.joining("\n\n")));
        return sb.toString();
    }

    // Mips
    public void toMips() {
        // .data 段
        for (IrGlobalVariable irGlobalVariable : globalVariables) {
            irGlobalVariable.toMips();
        }

        for (String key : new TreeSet<>(constStrMap.keySet())) {    // 按字符串编号升序输出
            constStrMap.get(key).toMips();
        }

        // .text 段
        new MipsAnnotation("Jump to main");
        new MipsJump(MipsJump.JumpType.JAL, "main");
        new MipsJump(MipsJump.JumpType.J, "end");

        for (IrFunc irFunc : functions) {
            irFunc.toMips();
        }

        // 结束
        new MipsLabel("end", MipsLabel.LabelType.FUNC_NAME);
        new MarsLi(Register.V0, 10);
        new MipsSyscall();
    }
}
