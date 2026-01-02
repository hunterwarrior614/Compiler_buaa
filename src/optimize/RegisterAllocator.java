package optimize;

import backend.mips.Register;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.*;

public class RegisterAllocator {
    private final List<Register> availableRegisters;
    private final Map<Register, IrValue> regToValue;
    private final Map<IrValue, Register> valueToReg;

    public RegisterAllocator(IrFunc func) {
        this.regToValue = new HashMap<>();
        this.valueToReg = func.getValueRegisterMap();
        this.availableRegisters = Register.getUsAbleRegisters();
    }

    public void run(IrBasicBlock entry) {
        Map<IrValue, IrInstr> lastUses = new HashMap<>();
        Set<IrValue> definedHere = new HashSet<>();
        Set<IrValue> killedHere = new HashSet<>();

        computeLastUses(entry, lastUses);
        assignRegistersInBlock(entry, lastUses, definedHere, killedHere);

        for (IrBasicBlock child : entry.getImmediateDominatedBlocks()) {
            processChild(child);
        }

        cleanupDefinitions(definedHere);
        restoreKilledGlobals(definedHere, killedHere);
    }

    private void computeLastUses(IrBasicBlock block, Map<IrValue, IrInstr> map) {
        for (IrInstr instr : block.getInstrs()) {
            for (IrValue op : instr.getUsees()) {
                map.put(op, instr);
            }
        }
    }

    private void assignRegistersInBlock(IrBasicBlock block, Map<IrValue, IrInstr> lastUses,
            Set<IrValue> defined, Set<IrValue> killed) {
        for (IrInstr instr : block.getInstrs()) {
            releaseRegisters(instr, lastUses, killed);
            allocateRegister(instr, defined);
        }
    }

    private void releaseRegisters(IrInstr instr, Map<IrValue, IrInstr> lastUses, Set<IrValue> killed) {
        if (instr instanceof PhiInstr)
            return;

        for (IrValue op : instr.getUsees()) {
            if (valueToReg.containsKey(op) &&
                    lastUses.get(op) == instr &&
                    !instr.getIrBasicBlock().getOutValueSet().contains(op)) {

                if (isUsedInSuccessorPhiOrMove(op, instr.getIrBasicBlock()))
                    continue;

                Register reg = valueToReg.get(op);
                regToValue.remove(reg);
                killed.add(op);
            }
        }
    }

    private boolean isUsedInSuccessorPhiOrMove(IrValue val, IrBasicBlock block) {
        for (IrBasicBlock succ : block.getNextBlocks()) {
            for (IrInstr instr : succ.getInstrs()) {
                if (instr instanceof MoveInstr move && move.getSrcValue() == val) {
                    return true;
                }
            }
        }
        return false;
    }

    private void allocateRegister(IrInstr instr, Set<IrValue> defined) {
        if (instr.getIrBaseType().isVoid() || valueToReg.containsKey(instr))
            return;

        defined.add(instr);
        for (Register reg : availableRegisters) {
            if (!regToValue.containsKey(reg)) {
                regToValue.put(reg, instr);
                valueToReg.put(instr, reg);
                break;
            }
        }
    }

    private void processChild(IrBasicBlock child) {
        Map<Register, IrValue> backup = new HashMap<>();

        Iterator<Map.Entry<Register, IrValue>> it = regToValue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Register, IrValue> entry = it.next();
            if (!child.getInValueSet().contains(entry.getValue())) {
                backup.put(entry.getKey(), entry.getValue());
                it.remove();
            }
        }

        run(child);

        regToValue.putAll(backup);
    }

    private void cleanupDefinitions(Set<IrValue> defined) {
        for (IrValue val : defined) {
            if (valueToReg.containsKey(val)) {
                Register reg = valueToReg.get(val);
                if (regToValue.get(reg) == val) {
                    regToValue.remove(reg);
                }
            }
        }
    }

    private void restoreKilledGlobals(Set<IrValue> defined, Set<IrValue> killed) {
        for (IrValue val : killed) {
            if (valueToReg.containsKey(val) && !defined.contains(val)) {
                regToValue.put(valueToReg.get(val), val);
            }
        }
    }
}
