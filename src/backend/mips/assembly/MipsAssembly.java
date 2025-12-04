package backend.mips.assembly;

import backend.mips.MipsBuilder;

public abstract class MipsAssembly {
    private final MipsType mipsType;

    public MipsAssembly(MipsType mipsType) {
        this.mipsType = mipsType;
        MipsBuilder.addMipsAssembly(this);
    }

    public MipsType getMipsType() {
        return mipsType;
    }
}
