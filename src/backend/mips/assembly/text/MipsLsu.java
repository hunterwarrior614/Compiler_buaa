package backend.mips.assembly.text;

import backend.mips.Register;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsLsu extends MipsAssembly {
    public enum LsuType {
        // load
        LW,
        // store
        SW;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final LsuType type;
    private final Register rd;
    private final Register base;
    private final int offset;

    public MipsLsu(LsuType type, Register rd, Register base, int offset) {
        super(MipsType.LSU);
        this.type = type;
        this.rd = rd;
        this.base = base;
        this.offset = offset;
    }

    @Override
    public String toString() {
        // lw $t1, -100($t2)
        // sw $t1, -100($t2)
        return type + " " + rd + ", " + offset + "(" + base + ")";
    }
}
