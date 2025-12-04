package backend.mips.assembly.text;

import backend.mips.Register;
import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsType;

public class MipsAlu extends MipsAssembly {
    public enum AluType {
        // R 型指令
        ADD, SUB, ADDU, SUBU,
        // 移位指令
        SLL,
        // I 型指令
        ADDI, ADDIU;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public boolean isInstrR() {
            return this == ADD || this == SUB || this == ADDU || this == SUBU;
        }

        public boolean isInstrI() {
            return this == ADDI || this == ADDIU;
        }

        public boolean isInstrShift() {
            return this == SLL;
        }
    }

    private final AluType aluType;
    private final Register rs;
    private final Register rt;
    private final Register rd;
    private final Integer immediate;


    // R 型指令
    public MipsAlu(AluType aluType, Register rd, Register rs, Register rt) {
        super(MipsType.ALU);
        this.aluType = aluType;
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
        immediate = null;

        if (!aluType.isInstrR()) {
            throw new RuntimeException("[ERROR] Unmatched AluType");
        }
    }

    // I 型指令 或 移位指令
    public MipsAlu(AluType aluType, Register rd, Register rs, int immediate) {
        super(MipsType.ALU);
        this.aluType = aluType;
        this.rd = rd;
        this.rs = rs;
        this.rt = null;
        this.immediate = immediate;

        if (!aluType.isInstrI() && !aluType.isInstrShift()) {
            throw new RuntimeException("[ERROR] Unmatched AluType");
        }
    }

    @Override
    public String toString() {

        return immediate == null ?
                aluType + " " + rd + ", " + rs + ", " + rt : // R型指令：add/sub $t1, $t2, $t3
                aluType + " " + rd + ", " + rs + ", " + immediate; // I 型指令：addi $t1, $t2, -100 或 移位指令：sll $t1, $t2, 2
    }


}
