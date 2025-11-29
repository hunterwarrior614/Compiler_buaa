package midend.llvm.instr;

import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class GetElemInstr extends IrInstr {
    public GetElemInstr(IrValue baseAddr, IrValue offset) {
        super(IrValueType.GETELEM_INSTR,
                // 返回值是一个指针类型，指向的值是INT32类型（此处是因为文法只会获得INT32类型元素）
                new IrBaseType(IrBaseType.TypeValue.POINTER, new IrBaseType(IrBaseType.TypeValue.INT32)),
                IrBuilder.getLocalVarName());
        usees.add(baseAddr);
        usees.add(offset);
    }

    private IrValue getBaseAddr() {
        return usees.get(0);
    }

    private IrValue getOffset() {
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
            sb.append(", i32 0, i32 ").append(getOffset().getName());
        }
        // 传入的baseAddr是INT32指针
        else {
            sb.append("i32, i32* ").append(baseAddr.getName()).append(", i32 ").append(getOffset().getName());
        }

        return sb.toString();
    }
}
