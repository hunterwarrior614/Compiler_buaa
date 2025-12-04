package midend.llvm.instr.io;

import backend.mips.Register;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.text.MipsSyscall;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class PrintIntInstr extends IOInstr {
    public PrintIntInstr(IrValue printValue) {
        super(IrValueType.OUTPUT_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "");
        usees.add(printValue);
    }

    public static String getDeclare() {
        return "declare void @putint(i32)";
    }

    private IrValue getPrintValue() {
        return usees.get(0);
    }

    @Override
    public String toString() {
        return "call void @putint(i32 " + getPrintValue().getName() + ")";
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        move $a0, $t0
        li $v0, 1
        syscall
         */
        IrValue printValue = getPrintValue();
        loadIrValue2Register(printValue, Register.A0); // 将打印值移到 $a0

        new MarsLi(Register.V0, 1);
        new MipsSyscall();
    }
}
