package optimize;

import backend.mips.Register;
import midend.llvm.constant.IrConst;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.instr.phi.ParallelCopyInstr;
import midend.llvm.instr.phi.PhiInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.*;

public class RemovePhi extends Optimizer {
    @Override
    public void Optimize() {
        this.lowerPhisToParallelCopies();
        this.lowerParallelCopiesToMoves();
    }

    private void lowerPhisToParallelCopies() {
        for (IrFunc func : irModule.getIrFuncs()) {
            List<IrBasicBlock> blocks = new ArrayList<>(func.getBasicBlocks());

            for (IrBasicBlock block : blocks) {
                boolean hasPhi = false;
                for (IrInstr instr : block.getInstrs()) {
                    if (instr instanceof PhiInstr) {
                        hasPhi = true;
                        break;
                    }
                }
                if (!hasPhi)
                    continue;

                List<ParallelCopyInstr> pcopies = new ArrayList<>();
                Map<IrBasicBlock, ParallelCopyInstr> pcopyMap = new HashMap<>();
                for (IrBasicBlock pred : block.getBeforeBlocks()) {
                    ParallelCopyInstr pcopy;
                    if (pred.getNextBlocks().size() > 1) {
                        pcopy = insertCopyOnSplitEdge(pred, block);
                    } else {
                        pcopy = insertCopyAtEnd(pred);
                    }
                    pcopies.add(pcopy);
                    pcopyMap.put(pred, pcopy);
                }

                Iterator<IrInstr> it = block.getInstrs().iterator();
                while (it.hasNext()) {
                    IrInstr instr = it.next();
                    if (instr instanceof PhiInstr phi) {
                        List<IrValue> incoming = phi.getUsees();
                        List<IrBasicBlock> phiPreds = phi.getBeforeBlocks();
                        for (int i = 0; i < incoming.size(); i++) {
                            IrBasicBlock pred = phiPreds.get(i);
                            if (pcopyMap.containsKey(pred)) {
                                pcopyMap.get(pred).addCopy(incoming.get(i), phi);
                            }
                        }
                        it.remove();
                    }
                }
            }
        }
    }

    private ParallelCopyInstr insertCopyAtEnd(IrBasicBlock block) {
        ParallelCopyInstr pcopy = new ParallelCopyInstr(block);
        block.addInstrBeforeJump(pcopy);
        return pcopy;
    }

    private ParallelCopyInstr insertCopyOnSplitEdge(IrBasicBlock pred, IrBasicBlock succ) {
        IrBasicBlock mid = IrBasicBlock.addMiddleBlock(pred, succ);
        ParallelCopyInstr pcopy = new ParallelCopyInstr(mid);
        mid.addInstrBeforeJump(pcopy);
        return pcopy;
    }

    private void lowerParallelCopiesToMoves() {
        for (IrFunc func : irModule.getIrFuncs()) {
            for (IrBasicBlock block : func.getBasicBlocks()) {
                if (block.haveParallelCopyInstr()) {
                    ParallelCopyInstr pcopy = block.getAndRemoveParallelCopyInstr();
                    sequentialize(pcopy, block);
                }
            }
        }
    }

    private void sequentialize(ParallelCopyInstr pcopy, IrBasicBlock block) {
        List<MoveInstr> moves = new ArrayList<>();
        List<IrValue> srcs = pcopy.getSrcList();
        List<IrValue> dsts = pcopy.getDstList();

        for (int i = 0; i < dsts.size(); i++) {
            moves.add(new MoveInstr(srcs.get(i), dsts.get(i), block));
        }

        List<MoveInstr> finalMoves = resolveConflicts(moves, block);

        for (MoveInstr move : finalMoves) {
            block.addInstrBeforeJump(move);
        }
    }

    private List<MoveInstr> resolveConflicts(List<MoveInstr> moves, IrBasicBlock block) {
        List<MoveInstr> result = new ArrayList<>(moves);
        List<MoveInstr> saves = new ArrayList<>();

        // Phase 1: Value conflicts (Circle)
        for (int i = 0; i < result.size(); i++) {
            IrValue dst = result.get(i).getDstValue();
            if (dst instanceof IrConst)
                continue;

            boolean conflict = false;
            for (int j = i + 1; j < result.size(); j++) {
                if (result.get(j).getSrcValue().equals(dst)) {
                    conflict = true;
                    break;
                }
            }

            if (conflict) {
                IrValue temp = new IrValue(dst.getIrValueType(), dst.getIrBaseType(), dst.getName() + "_tmp");
                saves.add(new MoveInstr(dst, temp, block));
                for (int j = i + 1; j < result.size(); j++) {
                    if (result.get(j).getSrcValue().equals(dst)) {
                        result.get(j).setSrcValue(temp);
                    }
                }
            }
        }

        result.addAll(0, saves);

        // Phase 2: Register conflicts
        Map<IrValue, Register> valToReg = block.getIrFunc().getValueRegisterMap();
        List<MoveInstr> regSaves = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            MoveInstr moveI = result.get(i);
            IrValue src = moveI.getSrcValue();
            Register rs = valToReg.get(src);
            if (rs == null)
                continue;

            boolean clobbered = false;
            for (int j = 0; j < i; j++) {
                IrValue dst = result.get(j).getDstValue();
                Register rd = valToReg.get(dst);
                if (rs.equals(rd)) {
                    clobbered = true;
                    break;
                }
            }

            if (clobbered) {
                IrValue temp = new IrValue(src.getIrValueType(), src.getIrBaseType(), src.getName() + "_reg_tmp");
                regSaves.add(new MoveInstr(src, temp, block));
                moveI.setSrcValue(temp);
            }
        }

        result.addAll(0, regSaves);
        return result;
    }
}
