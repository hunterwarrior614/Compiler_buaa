package midend.llvm.instr;

import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

public class AluInstr extends IrInstr {
    public enum AluType {
        ADD,
        SUB,
        MUL,
        SDIV,
        SREM,
    }

    private final AluType aluType;

    public AluInstr(String aluOp, IrValue lValue, IrValue rValue) {
        super(IrValueType.ALU_INSTR, new IrBaseType(IrBaseType.TypeValue.INT32), IrBuilder.getLocalVarName());
        this.aluType = translateAluType(aluOp);
        usees.add(lValue);
        usees.add(rValue);
    }

    private AluType translateAluType(String aluOp) {
        return switch (aluOp) {
            case "+" -> AluType.ADD;
            case "-" -> AluType.SUB;
            case "*" -> AluType.MUL;
            case "/" -> AluType.SDIV;
            case "%" -> AluType.SREM;
            default -> throw new IllegalArgumentException("[ERROR] Unknown aluOp]");
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
        return name + " = " + aluType.toString().toLowerCase() + " i32 " + getLValue().getName() + ", " + getRValue().getName();
    }
}
