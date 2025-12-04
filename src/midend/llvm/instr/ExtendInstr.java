package midend.llvm.instr;

import backend.mips.Register;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class ExtendInstr extends IrInstr {

    public ExtendInstr(IrValue originValue, IrBaseType targetType) {
        super(IrValueType.EXTEND_INSTR, targetType, IrBuilder.getLocalVarName());
        usees.add(originValue);
    }

    private IrValue getOriginValue() {
        return usees.get(0);
    }

    @Override
    public String toString() {
        // %5 = zext i1 %3 to i32
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = zext ");
        sb.append(getOriginValue().getIrBaseType()).append(" ").append(getOriginValue().getName());
        sb.append(" to ").append(irBaseType);
        return sb.toString();
    }

    // Mips
    @Override
    public void toMips() {
        super.toMips(); // 生成注释

        // mips无需位扩展，只需要将this与originValue映射
        IrValue originValue = getOriginValue();
        Register register = getRegisterOrK0ForIrValue(this);
        loadIrValue2Register(originValue, register);
        storeRegister2IrValue(register, this);
    }
}
