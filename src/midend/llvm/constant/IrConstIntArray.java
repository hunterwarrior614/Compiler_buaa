package midend.llvm.constant;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class IrConstIntArray extends IrConst {
    private final ArrayList<IrConstInt> valueList;
    private final int arrayLength;

    public IrConstIntArray(ArrayList<IrConstInt> valueList, int arrayLength) {
        super(IrValueType.CONST_DATA,
                // 返回值为数组（注意不是数组指针！GlobalVariable处才是数组指针）
                new IrBaseType(IrBaseType.TypeValue.INT_ARRAY, arrayLength),
                "constIntArray");
        this.valueList = valueList;
        this.arrayLength = arrayLength;
    }


    @Override
    public String toString() {
        // [3 x i32] [i32 1, i32 2, i32 3]
        // [20 x i32] zeroinitializer
        StringBuilder sb = new StringBuilder();
        sb.append(irBaseType).append(" ");

        if (valueList.isEmpty()) {
            sb.append("zeroinitializer");
        } else {
            sb.append("[");
            sb.append(valueList.stream().map(IrConstInt::toString).collect(Collectors.joining(", ")));
            // 将未指定值的数赋0
            String padding = ", i32 0";
            sb.append(padding.repeat(Math.max(0, arrayLength - valueList.size())));
            sb.append("]");
        }

        return sb.toString();
    }
}
