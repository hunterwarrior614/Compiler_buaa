package backend.mips.assembly.text;

import backend.mips.Register;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsCompare extends MipsAssembly {
    public enum CompareType {
        SLT,    // <
        SLE,    // <=
        SGT,    // >
        SGE,    // >=
        SEQ,    // ==
        SNE,    // !=
        ;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final CompareType compareType;
    private final Register rd;
    private final Register rs;
    private final Register rt;


    public MipsCompare(CompareType compareType, Register rd, Register rs, Register rt) {
        super(MipsType.COMPARE);
        this.compareType = compareType;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        // slt $t1, $t2, $t3
        return compareType + " " + rd + ", " + rs + ", " + rt;
    }
}
