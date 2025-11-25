package midend.llvm.instr.io;


import midend.llvm.constant.IrConstStr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class PrintStrInstr extends IOInstr {
    private final IrConstStr content;

    public PrintStrInstr(IrConstStr content) {
        super(IrValueType.OUTPUT_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "");    // TODO:name?
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
}
