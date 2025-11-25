package midend.llvm.instr.io;

import midend.llvm.instr.IrInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class IOInstr extends IrInstr {
    public IOInstr(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
    }
}
