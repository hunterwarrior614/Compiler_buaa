package midend.llvm.value;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsLabel;
import backend.mips.assembly.text.MipsLsu;
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

    public void addBasicBlock(IrBasicBlock basicBlock, IrBasicBlock nextBlock) {
        int index = basicBlocks.indexOf(nextBlock);
        basicBlocks.add(index, basicBlock);
    }

    public ArrayList<IrBasicBlock> getBasicBlocks() {
        return basicBlocks;
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

    public boolean isMainFunction() {
        return name.equals("@main");
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
            // 为形参在栈上分配空间，并将传入寄存器中的值保存到该空间
            Integer offset = MipsBuilder.allocateStackSpaceForIrValue(parameters.get(i));
            if (i < 3) {
                Register argReg = Register.getRegister(Register.A0.ordinal() + i);
                new MipsLsu(MipsLsu.LsuType.SW, argReg, Register.SP, offset);
            }
        }

        for (IrBasicBlock bb : basicBlocks) {
            bb.toMips();
        }
    }

    public HashMap<IrValue, Register> getValueRegisterMap() {
        return ValueRegisterMap;
    }
}
