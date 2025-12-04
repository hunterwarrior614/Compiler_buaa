package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.text.MipsJump;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class ReturnInstr extends IrInstr {
    public ReturnInstr(IrValue returnValue) {
        super(IrValueType.RETURN_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "return");
        addUsee(returnValue);
    }

    private IrValue getReturnValue() {
        return usees.isEmpty() ? null : usees.get(0);
    }

    @Override
    public String toString() {
        IrValue returnValue = getReturnValue();

        return "ret " + (returnValue == null ? "void" : "i32 " + returnValue.getName());
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        move $v0, $a0   -> 将返回值存储到 $v0 中
        jr $ra
         */
        IrValue returnValue = getReturnValue();
        // 若有返回值，则将返回值加载到 $v0 寄存器
        if (returnValue != null) {
            loadIrValue2Register(returnValue, Register.V0);
        }
        // 跳转到 $ra 处
        new MipsJump(MipsJump.JumpType.JR, Register.RA);
    }
}
