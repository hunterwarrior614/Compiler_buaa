package backend.mips.assembly.pseudo;

import backend.mips.Register;

public class MarsLi extends MipsMars {
    private final Register rd;
    private final int immediate;

    public MarsLi(Register rd, int immediate) {
        this.rd = rd;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        // li $t1, 100
        return "li " + rd + ", " + immediate;
    }
}
