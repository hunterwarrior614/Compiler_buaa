package midend.llvm.instr;

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
}
