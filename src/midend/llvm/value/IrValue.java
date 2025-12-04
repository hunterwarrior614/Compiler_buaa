package midend.llvm.value;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public abstract class IrValue {
    protected final IrValueType irValueType;
    protected final IrBaseType irBaseType;
    protected final String name;

    public IrValue(IrValueType irValueType, IrBaseType irBaseType, String name) {
        this.irValueType = irValueType;
        this.irBaseType = irBaseType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public IrBaseType getIrBaseType() {
        return irBaseType;
    }

    public IrBaseType.TypeValue getIrBaseTypeValue() {
        return irBaseType.getTypeValue();
    }

    // Mips
    public String getOriginName() {
        return name.substring(1);
    }
}
