package midend.llvm.instr;

import backend.mips.assembly.text.MipsJump;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;

public class JumpInstr extends IrInstr {
    public JumpInstr(IrBasicBlock jumpBlock) {
        super(IrValueType.JUMP_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "jump");
        usees.add(jumpBlock);
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
