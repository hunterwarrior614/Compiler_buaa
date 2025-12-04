package backend.mips.assembly.text;

import backend.mips.Register;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsMdu extends MipsAssembly {
    public enum MduType {
        // 乘除运算
        MULT, DIV,
        // 取值
        MFHI, MFLO, /*MTHI, MTLO*/;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final MduType mduType;
    private final Register rs;
    private final Register rt;
    private final Register rd;

    // mult/div $t1,$t2
    public MipsMdu(MduType mduType, Register rs, Register rt) {
        super(MipsType.MDU);
        this.mduType = mduType;
        this.rs = rs;
        this.rt = rt;
        this.rd = null;

        if (mduType != MduType.MULT && mduType != MduType.DIV) {
            throw new RuntimeException("[ERROR] Unmatched MduType: " + mduType);
        }
    }

    // mfhi/mflo/mthi/mtlo $t1
    public MipsMdu(MduType mduType, Register rd) {
        super(MipsType.MDU);
        this.mduType = mduType;
        this.rs = null;
        this.rt = null;
        this.rd = rd;

        if (mduType == MduType.MULT || mduType == MduType.DIV) {
            throw new RuntimeException("[ERROR] Unmatched MduType: " + mduType);
        }
    }

    @Override
    public String toString() {
        return rd == null ?
                mduType + " " + rs + ", " + rt :    // mult/div $t1, $t2
                mduType + " " + rd;                 // mfhi/mflo/mthi/mtlo $t1
    }
}
