package midend.llvm.instr.io;

import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class GetIntInstr extends IOInstr {
    public GetIntInstr() {
        super(IrValueType.INPUT_INSTR, new IrBaseType(IrBaseType.TypeValue.INT32), IrBuilder.getLocalVarName());
    }

    public static String getDeclare() {
        return "declare i32 @getint()";
    }

    @Override
    public String toString() {
        return name + " = call i32 @getint()";
    }
}
