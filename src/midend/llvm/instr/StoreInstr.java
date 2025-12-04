package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.text.MipsLsu;
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

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        sw $t1, -100($t2)
         */
        IrValue value = getValue();
        IrValue address = getAddress();
        // 为address和要存的value各分配一个寄存器
        Register valueRegister = getRegisterOrK0ForIrValue(value);
        Register addressRegister = getRegisterOrK1ForIrValue(address);
        // 将address和value加载到寄存器中
        loadIrValue2Register(value, valueRegister);
        loadIrValue2Register(address, addressRegister);

        new MipsLsu(MipsLsu.LsuType.SW, valueRegister, addressRegister, 0);
    }
}
