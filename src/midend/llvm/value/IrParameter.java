package midend.llvm.value;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class IrParameter extends IrValue {

    public IrParameter(IrBaseType paramType, String paramName) {
        super(IrValueType.ARGUMENT, paramType, paramName);
    }

    @Override
    public String toString() {
        if (irBaseType == null) {
            throw new RuntimeException("[ERROR] IrBaseType is null");
        }

        return getIrBaseType() + " " + name;
    }
}
