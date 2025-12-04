package midend.llvm.instr;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsAnnotation;
import backend.mips.assembly.pseudo.MarsLa;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.pseudo.MarsMove;
import backend.mips.assembly.text.MipsLsu;
import midend.llvm.IrBuilder;
import midend.llvm.constant.IrConstInt;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrGlobalVariable;
import midend.llvm.value.IrUser;
import midend.llvm.value.IrValue;

public class IrInstr extends IrUser {
    protected IrBasicBlock irBasicBlock;

    public IrInstr(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
        IrBuilder.addInstr(this);   // 每创建一个指令，就要加入当前BasicBlock
    }

    public void setIrBasicBlock(IrBasicBlock irBasicBlock) {
        this.irBasicBlock = irBasicBlock;
    }

    // Mips
    public void toMips() {
        new MipsAnnotation(this.toString());
    }

    // 将irValue对应的值加载到指定寄存器
    protected void loadIrValue2Register(IrValue irValue, Register targetRegister) {
        // 如果是整数常量，则直接用 li 指令加载
        if (irValue instanceof IrConstInt irConstInt) {
            new MarsLi(targetRegister, Integer.parseInt(irConstInt.getName()));
            return;
        }
        // 指针型变量（全局变量），直接从地址中加载
        if (irValue instanceof IrGlobalVariable irGlobalVariable) {
            new MarsLa(targetRegister, irGlobalVariable.getOriginName());
            return;
        }
        // 如果irValue对应的值已经在寄存器中，则直接从寄存器中加载
        Register registerOfIrValue = MipsBuilder.getRegisterOfIrValue(irValue);
        if (registerOfIrValue != null) {
            new MarsMove(targetRegister, registerOfIrValue);
            return;
        }

        // 否则从栈中获取
        Integer stackOffsetOfIrValue = MipsBuilder.getStackOffsetOfIrValue(irValue);
        if (stackOffsetOfIrValue == null) {
            // 若不在内存中，则先分配一块
            stackOffsetOfIrValue = MipsBuilder.allocateStackSpaceForIrValue(irValue);
        }
        new MipsLsu(MipsLsu.LsuType.LW, targetRegister, Register.SP, stackOffsetOfIrValue);
    }

    // 将valueRegister的值赋给irValue
    protected void storeRegister2IrValue(Register valueRegister, IrValue irValue) {
        Register registerOfIrValue = MipsBuilder.getRegisterOfIrValue(irValue);
        // 若 irValue 没有分配寄存器，则将 valueRegister 的值保存到栈中
        if (registerOfIrValue == null) {
            // TODO:是否要考虑irValue在栈中？
            int stackOffset = MipsBuilder.allocateStackSpaceForIrValue(irValue);
            new MipsLsu(MipsLsu.LsuType.SW, valueRegister, Register.SP, stackOffset);
        }
        // 否则直接将 valueRegister 的值移到 irValue 对应的寄存器
        else {
            new MarsMove(registerOfIrValue, valueRegister);
        }
    }

    // 获取irValue对应的寄存器，若没有则分配 K0
    protected Register getRegisterOrK0ForIrValue(IrValue irValue) {
        Register registerOfIrValue = MipsBuilder.getRegisterOfIrValue(irValue);
        return registerOfIrValue == null ? Register.K0 : registerOfIrValue;
    }

    // 获取irValue对应的寄存器，若没有则分配 K1
    protected Register getRegisterOrK1ForIrValue(IrValue irValue) {
        Register registerOfIrValue = MipsBuilder.getRegisterOfIrValue(irValue);
        return registerOfIrValue == null ? Register.K1 : registerOfIrValue;
    }
}
