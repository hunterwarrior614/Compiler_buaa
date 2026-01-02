package optimize;

import midend.llvm.constant.IrConstInt;
import midend.llvm.instr.AllocateInstr;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.LoadInstr;
import midend.llvm.instr.StoreInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.use.IrUse;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class InsertPhi {
    private final AllocateInstr targetAlloca;
    private final IrBasicBlock entryBlock;
    private final HashSet<IrInstr> defs;
    private final HashSet<IrInstr> uses;
    private final ArrayList<IrBasicBlock> defBlocks;
    private final Stack<IrValue> versionStack;

    public InsertPhi(AllocateInstr allocateInstr, IrBasicBlock entryBlock) {
        this.targetAlloca = allocateInstr;
        this.entryBlock = entryBlock;
        this.defs = new HashSet<>();
        this.uses = new HashSet<>();
        this.defBlocks = new ArrayList<>();
        this.versionStack = new Stack<>();
    }

    public void addPhi() {
        this.analyzeDefUse();
        this.placePhiNodes();
        this.renameVariables(entryBlock);
    }

    private void analyzeDefUse() {
        for (IrUse use : targetAlloca.getUseList()) {
            IrInstr user = (IrInstr) use.getUser();
            if (user instanceof LoadInstr) {
                uses.add(user);
            } else if (user instanceof StoreInstr store) {
                if (store.getAddress() == targetAlloca) {
                    defs.add(store);
                    if (!defBlocks.contains(store.getIrBasicBlock())) {
                        defBlocks.add(store.getIrBasicBlock());
                    }
                }
            }
        }
    }

    private void placePhiNodes() {
        HashSet<IrBasicBlock> phiBlocks = new HashSet<>();
        Queue<IrBasicBlock> workList = new LinkedList<>(defBlocks);

        while (!workList.isEmpty()) {
            IrBasicBlock block = workList.poll();

            for (IrBasicBlock frontier : block.getDominateFrontiers()) {
                if (!phiBlocks.contains(frontier)) {
                    this.createPhi(frontier);
                    phiBlocks.add(frontier);

                    if (!defBlocks.contains(frontier)) {
                        workList.add(frontier);
                    }
                }
            }
        }
    }

    private void createPhi(IrBasicBlock block) {
        PhiInstr phi = new PhiInstr(targetAlloca.getIrBaseType().getPointValueType(), block);
        block.addInstrFirst(phi);

        defs.add(phi);
        uses.add(phi);
    }

    private void renameVariables(IrBasicBlock block) {
        int pushCount = 0;

        Iterator<IrInstr> it = block.getInstrs().iterator();
        while (it.hasNext()) {
            IrInstr instr = it.next();

            if (instr == targetAlloca) {
                it.remove();
                continue;
            }

            if (instr instanceof StoreInstr store && defs.contains(store)) {
                versionStack.push(store.getValue());
                pushCount++;
                it.remove();
            } else if (instr instanceof LoadInstr load && uses.contains(load)) {
                IrValue val = getLiveVersion();
                load.replaceAllUsesWith(val);
                it.remove();
            } else if (instr instanceof PhiInstr phi && defs.contains(phi)) {
                versionStack.push(phi);
                pushCount++;
            }
        }

        for (IrBasicBlock succ : block.getNextBlocks()) {
            for (IrInstr instr : succ.getInstrs()) {
                if (instr instanceof PhiInstr phi && uses.contains(phi)) {
                    phi.ConvertBlockToValue(getLiveVersion(), block);
                } else {
                    if (!(instr instanceof PhiInstr))
                        break;
                }
            }
        }

        for (IrBasicBlock child : block.getImmediateDominatedBlocks()) {
            renameVariables(child);
        }

        while (pushCount > 0) {
            versionStack.pop();
            pushCount--;
        }
    }

    private IrValue getLiveVersion() {
        if (versionStack.isEmpty()) {
            return new IrConstInt(0);
        }
        return versionStack.peek();
    }
}
