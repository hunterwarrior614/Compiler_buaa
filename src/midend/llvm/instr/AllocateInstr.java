package midend.llvm.instr;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.text.MipsAlu;
import backend.mips.assembly.text.MipsLsu;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class AllocateInstr extends IrInstr {
    public AllocateInstr(IrBaseType valueType) {
        super(IrValueType.ALLOCATE_INSTR, new IrBaseType(IrBaseType.TypeValue.POINTER, valueType), IrBuilder.getLocalVarName());
    }

    @Override
    public String toString() {
        return name + " = alloca " + irBaseType.getPointValueType();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        addi $k0, $sp, -4   -> $k0 存储变量的地址
	    sw $k0, -8($sp)     -> 保存变量的地址（指针）
         */
        IrBaseType elemType = irBaseType.getPointValueType();
        // 先分配栈空间（不涉及 MIPS 操作）
        if (elemType.getTypeValue().equals(IrBaseType.TypeValue.INT_ARRAY)) {
            MipsBuilder.allocateStackSpace(4 * elemType.getLength());   // 数组
        } else {
            MipsBuilder.allocateStackSpace(4);  // 整数
        }

        // 再获取分配得到的指针（this）
        int pointerOffset = MipsBuilder.getCurrentStackOffset();
        Register register = MipsBuilder.getRegisterOfIrValue(this); // TODO:this一定没有register？
        // 如果该指针分配了寄存器，则直接将地址赋给该寄存器
        if (register != null) {
            new MipsAlu(MipsAlu.AluType.ADDIU, register, Register.SP, pointerOffset);
        }
        // 否则，将该指针保存到栈中
        else {
            new MipsAlu(MipsAlu.AluType.ADDIU, Register.K0, Register.SP, pointerOffset); // 先获取地址
            int pointerAddr = MipsBuilder.allocateStackSpaceForIrValue(this); // 为指针分配栈空间
            new MipsLsu(MipsLsu.LsuType.SW, Register.K0, Register.SP, pointerAddr); // 保存指针
        }
    }
}
