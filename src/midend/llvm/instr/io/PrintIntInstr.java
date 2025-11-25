package midend.llvm.instr.io;

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
}
