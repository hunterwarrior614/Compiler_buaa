package backend.mips.assembly.text;

import backend.mips.Register;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsBranch extends MipsAssembly {
    public enum BranchType {
        BEQ,    // ==
        BNE,    // !=
        BGTZ,   // > 0
        BLTZ,   // < 0
        BGEZ,   // >= 0
        BLEZ;   // <= 0

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final BranchType branchType;
    private final Register rs;
    private final Register rt;
    private final String label;

    // beq/bne $t1,$t2,label
    public MipsBranch(BranchType branchType, Register rs, Register rt, String label) {
        super(MipsType.BRANCH);
        this.branchType = branchType;
        this.rs = rs;
        this.rt = rt;
        this.label = label;

        if (branchType != BranchType.BEQ && branchType != BranchType.BNE) {
            throw new RuntimeException("[ERROR] Unmatched BranchType");
        }
    }

    // bgtz/bltz/bgez/blez $t1,label
    public MipsBranch(BranchType branchType, Register rs, String label) {
        super(MipsType.BRANCH);
        this.branchType = branchType;
        this.rs = rs;
        this.rt = null;
        this.label = label;

        if (branchType == BranchType.BEQ || branchType == BranchType.BNE) {
            throw new RuntimeException("[ERROR] Unmatched BranchType");
        }
    }

    @Override
    public String toString() {
        return (branchType == BranchType.BEQ || branchType == BranchType.BNE) ?
                branchType + " " + rs + ", " + rt + ", " + label :  // beq/bne $t1, $t2, label
                branchType + " " + rs + ", " + label;   // bgtz/bltz/bgez/blez $t1, label
    }
}
