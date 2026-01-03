package midend.llvm.value;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsLabel;
import backend.mips.assembly.text.MipsLsu;
import backend.mips.assembly.pseudo.MarsMove;
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
    private final HashMap<IrValue, Register> ValueRegisterMap; // value-register 分配表

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

    public ArrayList<IrParameter> getParams() {
        return parameters;
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
            if (i < 4) {
                Register argReg = Register.getRegister(Register.A0.ordinal() + i);
                new MipsLsu(MipsLsu.LsuType.SW, argReg, Register.SP, offset);

                // 如果该参数被分配了寄存器，则将参数寄存器的值移动到分配的寄存器中
                Register allocatedReg = ValueRegisterMap.get(parameters.get(i));
                if (allocatedReg != null) {
                    new MarsMove(allocatedReg, argReg);
                }
            } else {
                // 对于栈传递的参数，从 caller 的栈帧中加载
                // 参数在 caller 栈帧的底部，即当前 $sp + (i-4)*4
                int argOffset = (i - 4) * 4;

                Register allocatedReg = ValueRegisterMap.get(parameters.get(i));
                if (allocatedReg != null) {
                    // 如果分配了寄存器，直接加载到寄存器
                    new MipsLsu(MipsLsu.LsuType.LW, allocatedReg, Register.SP, argOffset);
                } else {
                    // 如果没有分配寄存器（溢出到栈），先加载到 K0，再保存到本地栈帧
                    new MipsLsu(MipsLsu.LsuType.LW, Register.K0, Register.SP, argOffset);
                    new MipsLsu(MipsLsu.LsuType.SW, Register.K0, Register.SP, offset);
                }
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
