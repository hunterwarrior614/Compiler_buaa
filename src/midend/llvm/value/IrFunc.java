package midend.llvm.value;

import midend.llvm.type.IrValueType;
import midend.llvm.type.IrBaseType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class IrFunc extends IrValue {
    private final ArrayList<IrParameter> parameters;
    private final ArrayList<IrBasicBlock> basicBlocks;

    public IrFunc(String name, IrBaseType returnType) {
        super(IrValueType.FUNCTION, returnType, name);
        parameters = new ArrayList<>();
        basicBlocks = new ArrayList<>();
    }


    public String getReturnTypeString() {
        if (irBaseType == null) {
            throw new RuntimeException("[ERROR] IrBaseType is null");
        }

        if (irBaseType.equals(new IrBaseType(IrBaseType.TypeValue.VOID))) {
            return "void";
        } else {
            return "i32";
        }
    }

    public void addBasicBlock(IrBasicBlock irBasicBlock) {
        basicBlocks.add(irBasicBlock);
    }

    public void addParameter(IrParameter irParameter) {
        parameters.add(irParameter);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 函数声明
        sb.append("define dso_local " + getReturnTypeString() + " " + name);
        sb.append("(");
        // 参数声明
        sb.append(parameters.stream().map(IrParameter::toString).collect(Collectors.joining(", ")));
        sb.append(") {\n");
        // 语句声明
        sb.append(basicBlocks.stream().map(IrBasicBlock::toString).collect(Collectors.joining("\n")));
        sb.append("\n}");
        return sb.toString();
    }
}
