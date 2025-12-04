package backend.mips.assembly.data;

public class MipsAsciiz extends MipsDataAssembly {
    private final String name;
    private final String content;

    public MipsAsciiz(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String toString() {
        // TODO:content.replace("\n", "\\n") ?
        return name + ": .asciiz \"" + content + "\"";
    }
}
