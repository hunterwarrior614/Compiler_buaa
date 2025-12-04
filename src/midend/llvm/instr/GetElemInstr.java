package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.text.MipsAlu;
import midend.llvm.IrBuilder;
import midend.llvm.constant.IrConstInt;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class GetElemInstr extends IrInstr {
    public GetElemInstr(IrValue baseAddr, IrValue index) {
        super(IrValueType.GETELEM_INSTR,
                // 返回值是一个指针类型，指向的值是INT32类型（此处是因为文法只会获得INT32类型元素）
                new IrBaseType(IrBaseType.TypeValue.POINTER, new IrBaseType(IrBaseType.TypeValue.INT32)),
                IrBuilder.getLocalVarName());
        usees.add(baseAddr);
        usees.add(index);
    }

    private IrValue getBaseAddr() {
        return usees.get(0);
    }

    private IrValue getIndex() {
        return usees.get(1);
    }

    @Override
    public String toString() {
        // <result> = getelementptr <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
        // %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = getelementptr inbounds ");
        IrValue baseAddr = getBaseAddr();
        // 传入的baseAddr是数组
        if (baseAddr.getIrBaseType().getPointValueTypeValue().equals(IrBaseType.TypeValue.INT_ARRAY)) {
            sb.append(baseAddr.getIrBaseType().getPointValueType()).append(", ").append(baseAddr.getIrBaseType()).append(" ").append(baseAddr.getName());
            sb.append(", i32 0, i32 ").append(getIndex().getName());
        }
        // 传入的baseAddr是INT32指针
        else {
            sb.append("i32, i32* ").append(baseAddr.getName()).append(", i32 ").append(getIndex().getName());
        }

        return sb.toString();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        $s0 = 数组基地址
        $t0 = 元素索引
        获取 array[index] 的地址

        sll $t1, $t0, 2      # 索引 × 4（因为整数占4字节），结果存入$t1
        add $t2, $s0, $t1    # 计算元素地址：基地址 + 偏移
         */
        IrValue baseAddr = getBaseAddr();
        IrValue index = getIndex();
        // 为基地址和索引分配寄存器
        Register baseRegister = getRegisterOrK0ForIrValue(baseAddr);
        Register indexRegister = getRegisterOrK1ForIrValue(index);
        Register addrResultRegister = getRegisterOrK1ForIrValue(this);
        // 加载基地址
        loadIrValue2Register(baseAddr, baseRegister);

        // 如果索引是常数，则直接获取地址偏移（4*index）
        if (index instanceof IrConstInt irConstInt) {
            new MipsAlu(MipsAlu.AluType.ADDI, addrResultRegister, baseRegister, 4 * Integer.parseInt(irConstInt.getName()));
        }
        // 如果索引是变量，则需要将变量通过移位指令获取地址偏移
        else {
            loadIrValue2Register(index, indexRegister);   // 加载索引
            new MipsAlu(MipsAlu.AluType.SLL, indexRegister, indexRegister, 2);    // 获取地址偏移
            new MipsAlu(MipsAlu.AluType.ADD, addrResultRegister, baseRegister, indexRegister);
        }

        storeRegister2IrValue(addrResultRegister, this);
    }
}
