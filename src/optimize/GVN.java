package optimize;

import midend.llvm.constant.IrConstInt;
import midend.llvm.instr.AluInstr;
import midend.llvm.instr.CompareInstr;
import midend.llvm.instr.GetElemInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.*;

public class GVN extends Optimizer {
    private final Map<String, IrValue> valueTable = new HashMap<>();

    @Override
    public void Optimize() {
        for (IrFunc func : irModule.getIrFuncs()) {
            if (!func.getBasicBlocks().isEmpty()) {
                valueTable.clear();
                run(func.getBasicBlocks().get(0));
            }
        }
    }

    private void run(IrBasicBlock block) {
        List<String> addedKeys = new ArrayList<>();
        Iterator<IrInstr> it = block.getInstrs().iterator();

        while (it.hasNext()) {
            IrInstr instr = it.next();

            // 1. Constant Folding
            IrValue folded = tryConstantFold(instr);
            if (folded != null) {
                instr.replaceAllUsesWith(folded);
                instr.removeAllUsees();
                it.remove();
                continue;
            }

            // 2. Value Numbering
            String key = computeHash(instr);
            if (key != null) {
                if (valueTable.containsKey(key)) {
                    IrValue existing = valueTable.get(key);
                    boolean canReplace = true;
                    if (existing instanceof IrInstr existingInstr) {
                        boolean isExpensive = isExpensive(instr);
                        boolean isLocal = existingInstr.getIrBasicBlock() == instr.getIrBasicBlock();
                        if (!isExpensive && !isLocal) {
                            canReplace = false;
                        }
                    }

                    if (canReplace) {
                        instr.replaceAllUsesWith(existing);
                        instr.removeAllUsees();
                        it.remove();
                    }
                } else {
                    valueTable.put(key, instr);
                    addedKeys.add(key);
                }
            }
        }

        for (IrBasicBlock child : block.getImmediateDominatedBlocks()) {
            run(child);
        }

        for (String key : addedKeys) {
            valueTable.remove(key);
        }
    }

    private boolean isExpensive(IrInstr instr) {
        if (instr instanceof AluInstr alu) {
            return alu.getAluType() == AluInstr.AluType.MUL ||
                    alu.getAluType() == AluInstr.AluType.SDIV ||
                    alu.getAluType() == AluInstr.AluType.SREM;
        }
        return false;
    }

    private IrValue tryConstantFold(IrInstr instr) {
        if (instr instanceof AluInstr alu) {
            IrValue l = alu.getUsees().get(0);
            IrValue r = alu.getUsees().get(1);
            if (l instanceof IrConstInt && r instanceof IrConstInt) {
                int lv = Integer.parseInt(l.getName());
                int rv = Integer.parseInt(r.getName());
                int res = 0;
                switch (alu.getAluType()) {
                    case ADD -> res = lv + rv;
                    case SUB -> res = lv - rv;
                    case MUL -> res = lv * rv;
                    case SDIV -> {
                        if (rv == 0)
                            return null;
                        res = lv / rv;
                    }
                    case SREM -> {
                        if (rv == 0)
                            return null;
                        res = lv % rv;
                    }
                }
                return new IrConstInt(res);
            }
            // Algebraic identities
            if (alu.getAluType() == AluInstr.AluType.ADD) {
                if (isZero(l))
                    return r;
                if (isZero(r))
                    return l;
            } else if (alu.getAluType() == AluInstr.AluType.SUB) {
                if (isZero(r))
                    return l;
                if (l == r)
                    return new IrConstInt(0);
            } else if (alu.getAluType() == AluInstr.AluType.MUL) {
                if (isOne(l))
                    return r;
                if (isOne(r))
                    return l;
                if (isZero(l))
                    return new IrConstInt(0);
                if (isZero(r))
                    return new IrConstInt(0);
            }
        }
        return null;
    }

    private boolean isZero(IrValue val) {
        return val instanceof IrConstInt && Integer.parseInt(val.getName()) == 0;
    }

    private boolean isOne(IrValue val) {
        return val instanceof IrConstInt && Integer.parseInt(val.getName()) == 1;
    }

    private String computeHash(IrInstr instr) {
        if (instr instanceof AluInstr alu) {
            String op = alu.getAluType().toString();
            IrValue l = alu.getUsees().get(0);
            IrValue r = alu.getUsees().get(1);
            if (alu.getAluType() == AluInstr.AluType.ADD || alu.getAluType() == AluInstr.AluType.MUL) {
                if (getHash(l) > getHash(r)) {
                    IrValue tmp = l;
                    l = r;
                    r = tmp;
                }
            }
            return "ALU:" + op + ":" + getHash(l) + ":" + getHash(r);
        } else if (instr instanceof CompareInstr cmp) {
            return "CMP:" + cmp.getCmpType() + ":" + getHash(cmp.getUsees().get(0)) + ":"
                    + getHash(cmp.getUsees().get(1));
        } else if (instr instanceof GetElemInstr gep) {
            return "GEP:" + getHash(gep.getUsees().get(0)) + ":" + getHash(gep.getUsees().get(1));
        }
        return null;
    }

    private int getHash(IrValue val) {
        if (val instanceof IrConstInt) {
            return Integer.parseInt(val.getName());
        }
        return val.hashCode();
    }
}
