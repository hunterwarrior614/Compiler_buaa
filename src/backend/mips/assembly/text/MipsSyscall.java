package backend.mips.assembly.text;

import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsSyscall extends MipsAssembly {
    public MipsSyscall() {
        super(MipsType.SYSCALL);
    }

    @Override
    public String toString() {
        return "syscall";
    }
}
