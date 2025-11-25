package midend.llvm.value;

public class IrLoop {
    private final IrBasicBlock stepBlock;
    private final IrBasicBlock followBlock;

    public IrLoop(IrBasicBlock stepBlock, IrBasicBlock followBlock) {
        this.stepBlock = stepBlock;
        this.followBlock = followBlock;
    }

    public IrBasicBlock getStepBlock() {
        return stepBlock;
    }

    public IrBasicBlock getFollowBlock() {
        return followBlock;
    }
}
