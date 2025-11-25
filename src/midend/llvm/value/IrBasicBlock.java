package midend.llvm.value;

import midend.llvm.instr.IrInstr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class IrBasicBlock extends IrValue {
    private final IrFunc irFunc;
    private final ArrayList<IrInstr> instrs;

    public IrBasicBlock(String name, IrFunc irFunc) {
        super(IrValueType.BASIC_BLOCK, new IrBaseType(IrBaseType.TypeValue.VOID), name);
        this.irFunc = irFunc;
        instrs = new ArrayList<>();
    }

    public void addInstr(IrInstr instr) {
        instrs.add(instr);
    }

    public boolean lastInstrIsReturn() {
        if (instrs.isEmpty()) {
            return false;
        }
        return instrs.get(instrs.size() - 1) instanceof ReturnInstr;
    }

    public boolean isEmpty() {
        return instrs.isEmpty();
    }

    public String getFuncName() {
        return irFunc.getName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(":\n\t");
        sb.append(instrs.stream().map(IrInstr::toString).collect(Collectors.joining("\n\t")));
        return sb.toString();
    }
}
