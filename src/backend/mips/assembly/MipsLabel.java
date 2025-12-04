package backend.mips.assembly;

public class MipsLabel extends MipsAssembly {
    public enum LabelType {
        FUNC_NAME,
        BLOCK_NAME,
    }

    private final LabelType type;
    private final String label;

    public MipsLabel(String label, LabelType type) {
        super(MipsType.LABEL);
        this.type = type;
        this.label = label;
    }

    @Override
    public String toString() {
        return (type == LabelType.FUNC_NAME ? "" : "\t") + label + ":";
    }
}
