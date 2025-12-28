package midend.llvm.instr;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import midend.llvm.constant.IrConstInt;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrValue;

public class MoveInstr extends IrInstr {
    public MoveInstr(IrValue srcValue, IrValue dstValue, IrBasicBlock irBasicBlock) {
        super(IrValueType.MOVE_INSTR, new IrBaseType(IrBaseType.TypeValue.VOID), "move", false);
        // srcValue 可能为 null（某些 phi 入口未被填充），兜底用 0 常量避免 NPE
        addUsee(srcValue);
        addUsee(dstValue);
        setIrBasicBlock(irBasicBlock);
    }

    public IrValue getSrcValue() {
        return usees.get(0);
    }

    public IrValue getDstValue() {
        return usees.get(1);
    }

    public void setSrcValue(IrValue srcValue) {
        getSrcValue().deleteUser(this);
        usees.set(0, srcValue);
    }

    @Override
    public String toString() {
        return "move " + getDstValue().getName() + ", " + getSrcValue().getName();
    }

    @Override
    public void toMips() {
        super.toMips(); // 生成注释

        IrValue srcValue = getSrcValue();
        IrValue dstValue = getDstValue();
        Register srcRegister = MipsBuilder.getRegisterOfIrValue(srcValue);
        Register dstRegister = MipsBuilder.getRegisterOfIrValue(dstValue);

        // 不需要move
        if (srcRegister != null && srcRegister.equals(dstRegister)) {
            return;
        }
        dstRegister = getRegisterOrK0ForIrValue(dstValue);
        loadIrValue2Register(srcValue, dstRegister);
        storeRegister2IrValue(dstRegister, dstValue);
    }
}
