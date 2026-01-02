package backend.mips.assembly.pseudo;

import backend.mips.Register;

public class MarsMove extends MipsMars {
    private final Register dst;
    private final Register src;

    public MarsMove(Register dst, Register src) {
        this.dst = dst;
        this.src = src;
    }


    @Override
    public String toString() {
        // move $t1, $t2 -> addu $t1, $zero, $t2
        return (dst == src ? "# " : "") + "addu " + dst + ", $zero, " + src;
    }
}
