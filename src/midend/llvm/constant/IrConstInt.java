package midend.llvm.constant;

import backend.mips.assembly.data.MipsWord;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class IrConstInt extends IrConst {

    public IrConstInt(int value) {
        super(IrValueType.CONST_DATA, new IrBaseType(IrBaseType.TypeValue.INT32), String.valueOf(value));
    }

    @Override
    public String toString() {
        return "i32 " + name;
    }

    // Mips
    @Override
    public void toMips(String label) {
        new MipsWord(label, Integer.parseInt(name));
    }
}
