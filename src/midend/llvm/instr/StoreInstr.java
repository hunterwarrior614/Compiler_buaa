package midend.llvm.instr;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class StoreInstr extends IrInstr {
    public StoreInstr(IrValue value, IrValue address) {
        super(IrValueType.STORE_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "store");
        this.addUsee(value);
        this.addUsee(address);
    }

    private IrValue getValue() {
        return usees.get(0);
    }

    private IrValue getAddress() {
        return usees.get(1);
    }

    @Override
    public String toString() {
        // store i32 %0, i32* %3
        // store i32* %1, i32** %4
        StringBuilder sb = new StringBuilder();
        sb.append("store ");
        sb.append(getValue().getIrBaseType()).append(" ").append(getValue().getName()).append(", ");
        sb.append(getAddress().getIrBaseType()).append(" ").append(getAddress().getName());
        return sb.toString();
    }
}
