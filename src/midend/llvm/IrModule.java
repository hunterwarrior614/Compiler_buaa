package midend.llvm;

import midend.llvm.constant.IrConstStr;
import midend.llvm.instr.io.GetIntInstr;
import midend.llvm.instr.io.PrintIntInstr;
import midend.llvm.instr.io.PrintStrInstr;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrGlobalVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IrModule {
    private final ArrayList<String> declares;   // 用于库函数声明
    private final HashMap<String, IrConstStr> constStrMap;  // 程序中出现的字符串
    private final ArrayList<IrGlobalVariable> globalVariables; // 全局变量声明
    private final ArrayList<IrFunc> functions;  // 函数声明

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
}
