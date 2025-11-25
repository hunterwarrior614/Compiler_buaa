package midend.llvm.instr;


import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class LoadInstr extends IrInstr {
    public LoadInstr(IrValue pointer) {
        super(IrValueType.LOAD_INSTR,
                pointer.getIrBaseType().getPointValueType(),   // 返回值类型为指针类型对应的值类型
                IrBuilder.getLocalVarName());
        usees.add(pointer);
    }

    private IrValue getPointer() {
        return usees.get(0);
    }

    @Override
    public String toString() {
        // %5 = load i32, i32* %3
        // %6 = load i32*, i32** %4
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = load ");
        IrValue pointer = getPointer();
        sb.append(pointer.getIrBaseType().getPointValueType()).append(", ");
        sb.append(pointer.getIrBaseType()).append(" ").append(pointer.getName());
        return sb.toString();
    }
}
