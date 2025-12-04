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
        SGT,    // >
        SGE,    // >=
        SLT,    // <
        SLE;    // <=

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
            case EQ -> new MipsCompare(MipsCompare.CompareType.SEQ, resultRegister, lRegister, rRegister);
            case NE -> new MipsCompare(MipsCompare.CompareType.SNE, resultRegister, lRegister, rRegister);
            case SLE -> new MipsCompare(MipsCompare.CompareType.SLE, resultRegister, lRegister, rRegister);
            case SGE -> new MipsCompare(MipsCompare.CompareType.SGE, resultRegister, lRegister, rRegister);
            case SGT -> new MipsCompare(MipsCompare.CompareType.SGT, resultRegister, lRegister, rRegister);
            case SLT -> new MipsCompare(MipsCompare.CompareType.SLT, resultRegister, lRegister, rRegister);
        }

        storeRegister2IrValue(resultRegister, this);
    }
}
