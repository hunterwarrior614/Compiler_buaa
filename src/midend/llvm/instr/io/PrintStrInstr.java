package midend.llvm.instr.io;


import backend.mips.Register;
import backend.mips.assembly.pseudo.MarsLa;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.text.MipsSyscall;
import midend.llvm.constant.IrConstStr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class PrintStrInstr extends IOInstr {
    private final IrConstStr content;

    public PrintStrInstr(IrConstStr content) {
        super(IrValueType.OUTPUT_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "");
        this.content = content;
    }

    public static String getDeclare() {
        return "declare void @putstr(i8*)";
    }

    @Override
    public String toString() {
        return "call void @putstr(i8* getelementptr inbounds (" +
                content.getIrBaseType() + ", " + content.getIrBaseType() + "* " + content.getName()
                + ", i64 0,i64 0))";
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        li $v0, 4
        la $a0, str
        syscall
         */
        new MarsLi(Register.V0, 4);
        new MarsLa(Register.A0, content.getOriginName());
        new MipsSyscall();
    }
}
