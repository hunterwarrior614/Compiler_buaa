package midend.llvm.instr;

import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrUser;

public class IrInstr extends IrUser {
    protected IrBasicBlock irBasicBlock;

    public IrInstr(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
        IrBuilder.addInstr(this);   // 每创建一个指令，就要加入当前BasicBlock
    }

    public void setIrBasicBlock(IrBasicBlock irBasicBlock) {
        this.irBasicBlock = irBasicBlock;
    }
}
