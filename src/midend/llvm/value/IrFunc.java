package midend.llvm.value;

import midend.llvm.IrBuilder;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.ReturnInstr;
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

        if (irBaseType.getTypeValue().equals(IrBaseType.TypeValue.VOID)) {
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

    public void checkReturn() {
        IrBasicBlock currentBlock = IrBuilder.getCurrentIrBasicBlock();
        if (!currentBlock.lastInstrIsReturn()) {
            new ReturnInstr(null);
        }
    }

    public void checkEmptyBasicBlocks() {
        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            IrBasicBlock bb = basicBlocks.get(i);
            // 若出现空基本块，则插入一条跳转到下一个基本块的指令
            if (bb.isEmpty()) {
                bb.addInstr(new JumpInstr(basicBlocks.get(i + 1)));
            }
        }
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
