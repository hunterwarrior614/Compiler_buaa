package midend.llvm.instr;

import backend.mips.assembly.text.MipsJump;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;

public class JumpInstr extends IrInstr {
    public JumpInstr(IrBasicBlock jumpBlock) {
        super(IrValueType.JUMP_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "jump");
        addUsee(jumpBlock);
    }

    public JumpInstr(IrBasicBlock jumpBlock, IrBasicBlock createBlock) {
        super(IrValueType.JUMP_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "jump", false);
        addUsee(jumpBlock);
        setIrBasicBlock(createBlock);
    }

    public void setJumpTarget(IrBasicBlock jumpBlock) {
        // 删除原先的使用关系
        this.getJumpBlock().deleteUser(this);
        this.usees.clear();
        this.addUsee(jumpBlock);
    }

    public IrBasicBlock getJumpBlock() {
        return (IrBasicBlock) usees.get(0);
    }

    @Override
    public String toString() {
        return "br label %" + getJumpBlock().getName();
    }

    // Mips
    public void toMips() {
        super.toMips();

        new MipsJump(MipsJump.JumpType.J, getJumpBlock().getName());
    }
}
