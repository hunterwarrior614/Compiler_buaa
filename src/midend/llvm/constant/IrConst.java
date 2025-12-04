package midend.llvm.constant;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public abstract class IrConst extends IrValue {
    public IrConst(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
    }

    public void toMips(String label) {
    }

    public void toMips() {
    }
}
