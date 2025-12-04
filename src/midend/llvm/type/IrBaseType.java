package midend.llvm.type;

public class IrBaseType {
    public enum TypeValue {
        INT1,
        INT32,
        VOID,

        STRING,
        INT_ARRAY,

        POINTER
    }

    private final TypeValue typeValue;
    private final int length;
    private final IrBaseType pointValueType;   // 指针指向的值类型

    public IrBaseType(TypeValue typeValue) {
        this.typeValue = typeValue;
        length = 0;
        pointValueType = null;
    }

    public IrBaseType(TypeValue typeValue, int length) {
        this.typeValue = typeValue;
        this.length = length;
        pointValueType = null;
    }

    public IrBaseType(TypeValue typeValue, IrBaseType pointValueType) {
        this.typeValue = typeValue;
        this.length = 0;
        this.pointValueType = pointValueType;
    }

    public IrBaseType(TypeValue typeValue, IrBaseType pointValueType, int length) {
        this.typeValue = typeValue;
        this.length = length;
        this.pointValueType = pointValueType;
    }

    public IrBaseType getPointValueType() {
        if (pointValueType == null) {
            throw new RuntimeException("[ERROR] typeValue is null");
        }
        return pointValueType;
    }

    public TypeValue getPointValueTypeValue() {
        if (pointValueType == null) {
            throw new RuntimeException("[ERROR] typeValue is null");
        }
        return pointValueType.typeValue;
    }

    public TypeValue getTypeValue() {
        return typeValue;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return switch (this.typeValue) {
            case INT1 -> "i1";
            case INT32 -> "i32";
            case VOID -> "void";
            case STRING -> "[" + length + " x i8]";
            case INT_ARRAY -> "[" + length + " x i32]";
            case POINTER -> pointValueType + "*";
        };
    }
}
