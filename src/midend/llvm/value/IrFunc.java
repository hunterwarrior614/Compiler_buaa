package midend.llvm.value;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsLabel;
import midend.llvm.IrBuilder;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.type.IrValueType;
import midend.llvm.type.IrBaseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class IrFunc extends IrValue {
    private final ArrayList<IrParameter> parameters;
    private final ArrayList<IrBasicBlock> basicBlocks;
    private final HashMap<IrValue, Register> ValueRegisterMap;  // value-register 分配表

    public IrFunc(String name, IrBaseType returnType) {
        super(IrValueType.FUNCTION, returnType, name);
        parameters = new ArrayList<>();
        basicBlocks = new ArrayList<>();
        ValueRegisterMap = new HashMap<>();
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

    // Mips
    public void toMips() {
        new MipsLabel(getOriginName(), MipsLabel.LabelType.FUNC_NAME); // 函数标签
        MipsBuilder.setCurrentFunction(this);

        for (int i = 0; i < parameters.size(); i++) {
            // 将前四个形参映射到 $a0-$a3（这里只需要完成映射就行，无需为irParameter分配空间）
            if (i < 3) {
                MipsBuilder.mapIrParameter2Register(parameters.get(i), Register.getRegister(Register.A0.ordinal() + i));
            }
            MipsBuilder.allocateStackSpaceForIrValue(parameters.get(i));    // 要在栈上分配空间
        }

        for (IrBasicBlock bb : basicBlocks) {
            bb.toMips();
        }
    }

    public HashMap<IrValue, Register> getValueRegisterMap() {
        return ValueRegisterMap;
    }
}
