package midend.llvm.instr.io;

import backend.mips.Register;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.text.MipsSyscall;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class GetIntInstr extends IOInstr {
    public GetIntInstr() {
        super(IrValueType.INPUT_INSTR, new IrBaseType(IrBaseType.TypeValue.INT32), IrBuilder.getLocalVarName());
    }

    public static String getDeclare() {
        return "declare i32 @getint()";
    }

    @Override
    public String toString() {
        return name + " = call i32 @getint()";
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释
        /*
        li $v0, 5
        syscall     -> 读取的整数自动存储在 $v0 中
         */
        new MarsLi(Register.V0, 5);
        new MipsSyscall();
        storeRegister2IrValue(Register.V0, this);   // 将 $v0 的值赋给（捆绑）当前 irValue
    }
}
