package midend.llvm.instr.phi;

import midend.llvm.IrBuilder;
import midend.llvm.instr.IrInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.use.IrUse;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrValue;

import java.util.ArrayList;

public class PhiInstr extends IrInstr {
    private final ArrayList<IrBasicBlock> beforeBlocks;

    public PhiInstr(IrBaseType type, IrBasicBlock irBasicBlock) {
        super(IrValueType.PHI_INSTR, type, IrBuilder.getLocalVarName(irBasicBlock.getIrFunc()), false);
        setIrBasicBlock(irBasicBlock);

        beforeBlocks = new ArrayList<>(irBasicBlock.getBeforeBlocks());
        // 填充相应的value，等待后续替换
        for (int i = 0; i < beforeBlocks.size(); i++) {
            addUsee(null);
        }
    }

    public void ConvertBlockToValue(IrValue value, IrBasicBlock block) {
        int index = beforeBlocks.indexOf(block);
        // 进行相应的值替换：原先只会是null
        usees.set(index, value);
        // 添加use关系
        value.addUse(new IrUse(this, value));
    }

    public void removeBlock(IrBasicBlock block) {
        int index = beforeBlocks.indexOf(block);
        if (index != -1) {
            beforeBlocks.remove(index);
            IrValue value = usees.get(index);
            if (value != null) {
                value.deleteUser(this);
            }
            usees.remove(index);
        }
    }

    public void replaceBlock(IrBasicBlock oldBlock, IrBasicBlock newBlock) {
        int index;
        if (this.beforeBlocks.contains(newBlock)) {
            index = this.beforeBlocks.indexOf(newBlock);
            this.beforeBlocks.remove(index);
            this.usees.remove(index);
        }

        index = this.beforeBlocks.indexOf(oldBlock);
        if (index != -1) {
            this.beforeBlocks.set(index, newBlock);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = phi ").append(irBaseType).append(" ");
        for (int i = 0; i < beforeBlocks.size(); i++) {
            sb.append("[ ").append(usees.get(i).getName()).append(", %").append(beforeBlocks.get(i).getName()).append(" ]");
            if (i != usees.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public void toMips() {
        throw new RuntimeException("[ERROR] phi should not exist!");
    }
}
