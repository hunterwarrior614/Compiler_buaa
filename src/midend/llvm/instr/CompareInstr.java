package midend.llvm.instr;

import backend.mips.Register;
import backend.mips.assembly.text.MipsCompare;
import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class CompareInstr extends IrInstr {
    public enum CompareOpType {
        EQ,
        NE,
        SGT, // >
        SGE, // >=
        SLT, // <
        SLE; // <=

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final CompareOpType compType;

    public CompareInstr(String compareOp, IrValue lValue, IrValue rValue) {
        super(IrValueType.COMPARE_INSTR, new IrBaseType(IrBaseType.TypeValue.INT1), IrBuilder.getLocalVarName());
        compType = translateCompType(compareOp);
        addUsee(lValue);
        addUsee(rValue);
    }

    private CompareOpType translateCompType(String compareOp) {
        return switch (compareOp) {
            case "==" -> CompareOpType.EQ;
            case "!=" -> CompareOpType.NE;
            case ">" -> CompareOpType.SGT;
            case ">=" -> CompareOpType.SGE;
            case "<" -> CompareOpType.SLT;
            case "<=" -> CompareOpType.SLE;
            default -> throw new Error("[ERROR] Invalid compareOp]");
        };
    }

    public CompareOpType getCmpType() {
        return compType;
    }

    private IrValue getLValue() {
        return usees.get(0);
    }

    private IrValue getRValue() {
        return usees.get(1);
    }

    @Override
    public String toString() {
        // %3 = icmp ne i32 %2, 0
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = icmp ").append(compType).append(" ");
        IrValue lValue = getLValue();
        sb.append(lValue.getIrBaseTypeValue().equals(IrBaseType.TypeValue.INT1) ? "i1 " : "i32 ")
                .append(getLValue().getName()).append(", ").append(getRValue().getName());
        return sb.toString();
    }

    // Mips
    public void toMips() {
        super.toMips(); // 生成注释

        IrValue lValue = getLValue();
        IrValue rValue = getRValue();

        Register lRegister = getRegisterOrK0ForIrValue(lValue);
        Register rRegister = getRegisterOrK1ForIrValue(rValue);
        Register resultRegister = getRegisterOrK0ForIrValue(this);

        loadIrValue2Register(lValue, lRegister);
        loadIrValue2Register(rValue, rRegister);

        switch (compType) {
            // slt $t1, $t2, $t3
            case EQ -> {
                // xor $rd, $rs, $rt
                new backend.mips.assembly.text.MipsAlu(backend.mips.assembly.text.MipsAlu.AluType.XOR, resultRegister, lRegister, rRegister);
                // sltiu $rd, $rd, 1
                new backend.mips.assembly.text.MipsAlu(backend.mips.assembly.text.MipsAlu.AluType.SLTIU, resultRegister, resultRegister, 1);
            }
            case NE -> {
                // xor $rd, $rs, $rt
                new backend.mips.assembly.text.MipsAlu(backend.mips.assembly.text.MipsAlu.AluType.XOR, resultRegister, lRegister, rRegister);
                // sltu $rd, $zero, $rd
                new backend.mips.assembly.text.MipsAlu(backend.mips.assembly.text.MipsAlu.AluType.SLTU, resultRegister, Register.ZERO, resultRegister);
            }
            case SLE -> {
                // slt $rd, $rt, $rs
                new MipsCompare(MipsCompare.CompareType.SLT, resultRegister, rRegister, lRegister);
                // xori $rd, $rd, 1
                new backend.mips.assembly.text.MipsAlu(backend.mips.assembly.text.MipsAlu.AluType.XORI, resultRegister, resultRegister, 1);
            }
            case SGE -> {
                // slt $rd, $rs, $rt
                new MipsCompare(MipsCompare.CompareType.SLT, resultRegister, lRegister, rRegister);
                // xori $rd, $rd, 1
                new backend.mips.assembly.text.MipsAlu(backend.mips.assembly.text.MipsAlu.AluType.XORI, resultRegister, resultRegister, 1);
            }
            case SGT -> {
                // slt $rd, $rt, $rs
                new MipsCompare(MipsCompare.CompareType.SLT, resultRegister, rRegister, lRegister);
            }
            case SLT -> new MipsCompare(MipsCompare.CompareType.SLT, resultRegister, lRegister, rRegister);
        }

        storeRegister2IrValue(resultRegister, this);
    }
}
