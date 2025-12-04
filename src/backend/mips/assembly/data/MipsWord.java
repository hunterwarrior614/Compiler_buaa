package backend.mips.assembly.data;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MipsWord extends MipsDataAssembly {
    private final String name;
    private final ArrayList<Integer> valueList;
    private final int arraySize;

    public MipsWord(String name, int value) {
        this.name = name;
        this.valueList = new ArrayList<>();
        valueList.add(value);
        arraySize = 0;
    }

    public MipsWord(String name, ArrayList<Integer> valueList, int arraySize) {
        this.name = name;
        // 事实上，要么valueList.size() == arraySize，要么valueList为空，因为LLVM IR生成时若只进行一部分的赋值会补全所有的赋值
        this.valueList = valueList;
        this.arraySize = arraySize;
    }


    @Override
    public String toString() {
        // a: .word 2
        // b: .word 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        StringBuilder sb = new StringBuilder();
        sb.append(name + ": .word ");
        if (!valueList.isEmpty()) {
            sb.append(valueList.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        } else {
            sb.append("0:" + arraySize);
        }

        return sb.toString();
    }
}
