package midend.llvm.use;

import midend.llvm.value.IrValue;

public class IrUse {
    private final IrUser user;
    private final IrValue value;

    public IrUse(IrUser user, IrValue value) {
        this.user = user;
        this.value = value;
    }

    public IrUser getUser() {
        return this.user;
    }

    public IrValue getValue() {
        return this.value;
    }
}
