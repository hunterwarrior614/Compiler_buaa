package backend.mips.assembly.text;

import backend.mips.Register;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsJump extends MipsAssembly {
    public enum JumpType {
        J, JAL, JR;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final JumpType jumpType;
    private final String targetLabel;
    private final Register rd;

    // jal target
    // j target
    public MipsJump(JumpType jumpType, String targetLabel) {
        super(MipsType.JUMP);
        this.jumpType = jumpType;
        this.targetLabel = targetLabel;
        rd = null;

        if (jumpType == JumpType.JR) {
            throw new RuntimeException("[ERROR] Unmatched JumpType");
        }
    }

    // jr $t1
    public MipsJump(JumpType jumpType, Register rd) {
        super(MipsType.JUMP);
        this.jumpType = jumpType;
        this.targetLabel = null;
        this.rd = rd;

        if (jumpType != JumpType.JR) {
            throw new RuntimeException("[ERROR] Unmatched JumpType");
        }
    }

    @Override
    public String toString() {
        return switch (jumpType) {
            case J, JAL -> jumpType + " " + targetLabel; // jal/j target
            case JR -> jumpType + " " + rd;    // jr $t1
        };
    }
}
