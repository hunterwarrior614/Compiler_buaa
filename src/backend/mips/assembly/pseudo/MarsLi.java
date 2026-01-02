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
        if (immediate >= 0 && immediate <= 65535) {
            return "ori " + rd + ", $zero, " + immediate;
        } else if (immediate >= -32768 && immediate <= 32767) {
            return "addiu " + rd + ", $zero, " + immediate;
        } else {
            int upper = (immediate >>> 16) & 0xFFFF;
            int lower = immediate & 0xFFFF;
            return "lui " + rd + ", " + upper + "\n\t" +
                   "ori " + rd + ", " + rd + ", " + lower;
        }
    }
}
