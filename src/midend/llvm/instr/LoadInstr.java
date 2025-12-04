package midend.llvm.instr;


import backend.mips.Register;
import backend.mips.assembly.text.MipsLsu;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class LoadInstr extends IrInstr {
    public LoadInstr(IrValue address) {
        super(IrValueType.LOAD_INSTR,
                address.getIrBaseType().getPointValueType(),   // 返回值类型为指针类型对应的值类型
                IrBuilder.getLocalVarName());
        usees.add(address);
    }

    private IrValue getAddress() {
        return usees.get(0);
    }

    @Override
    public String toString() {
        // %5 = load i32, i32* %3
        // %6 = load i32*, i32** %4
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = load ");
        IrValue address = getAddress();
        sb.append(address.getIrBaseType().getPointValueType()).append(", ");
        sb.append(address.getIrBaseType()).append(" ").append(address.getName());
        return sb.toString();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        lw $t1, -100($t2)
         */
        IrValue address = getAddress();
        // 为address和加载值各分配一个寄存器
        Register addressRegister = getRegisterOrK0ForIrValue(address);
        Register valueRegister = getRegisterOrK0ForIrValue(this);
        // 将address加载到addressRegister
        loadIrValue2Register(address, addressRegister);
        // 调用Mips加载指令
        new MipsLsu(MipsLsu.LsuType.LW, valueRegister, addressRegister, 0);
        // 最后将valueRegister赋给this
        storeRegister2IrValue(valueRegister, this);
    }
}
