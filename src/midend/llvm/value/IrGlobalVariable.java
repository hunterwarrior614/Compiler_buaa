package midend.llvm.value;

import midend.llvm.constant.IrConst;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class IrGlobalVariable extends IrUser {
    private final IrConst globalVariable;   // 默认为 0 或 {0...0}

    public IrGlobalVariable(String name, IrBaseType irBaseType, IrConst globalVariable) {
        super(IrValueType.GLOBAL_VARIABLE, irBaseType, name);
        this.globalVariable = globalVariable;
    }

    @Override
    public String toString() {
        return name + " = dso_local global " + globalVariable;
    }
}
