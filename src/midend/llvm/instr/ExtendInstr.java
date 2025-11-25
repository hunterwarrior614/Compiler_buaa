package midend.llvm.instr;

import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class ExtendInstr extends IrInstr {
    private final IrBaseType targetType;

    public ExtendInstr(IrValue originValue, IrBaseType targetType) {
        super(IrValueType.EXTEND_INSTR, new IrBaseType(IrBaseType.TypeValue.INT32), IrBuilder.getLocalVarName());
        this.targetType = targetType;
        usees.add(originValue);
    }

    private IrValue getOriginValue() {
        return usees.get(0);
    }

    @Override
    public String toString() {
        // %5 = zext i8 %3 to i32
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = zext ");
        sb.append(getOriginValue().getIrBaseType()).append(" ").append(getOriginValue().getName()).append(" to ");
        sb.append(targetType);
        return sb.toString();
    }
}
