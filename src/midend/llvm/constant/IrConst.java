package midend.llvm.constant;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class IrConst extends IrValue {
    public IrConst(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
    }
}
