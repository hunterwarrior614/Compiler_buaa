package backend.mips;

import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.MipsLabel;

import java.util.ArrayList;

public class MipsModule {
    private final ArrayList<MipsAssembly> dataSegment;
    private final ArrayList<MipsAssembly> textSegment;

    public MipsModule() {
        dataSegment = new ArrayList<>();
        textSegment = new ArrayList<>();
    }

    public void addToDataSeg(MipsAssembly assembly) {
        dataSegment.add(assembly);
    }

    public void addToTextSeg(MipsAssembly assembly) {
        textSegment.add(assembly);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // .data 段
        sb.append(".data\n");
        for (MipsAssembly dataAssembly : dataSegment) {
            sb.append("\t").append(dataAssembly).append("\n");
        }
        sb.append("\n");
        // .text 段
        sb.append(".text\n");
        for (MipsAssembly textAssembly : textSegment) {
            sb.append(textAssembly instanceof MipsLabel ? "" : "\t").append(textAssembly).append("\n");
        }
        return sb.toString();
    }
}
