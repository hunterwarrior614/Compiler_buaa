package midend.llvm.instr;

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
}
