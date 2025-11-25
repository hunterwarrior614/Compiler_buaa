package midend.symbol;

import frontend.parser.ast.exp.ConstExp;
import midend.llvm.type.IrBaseType;

import java.util.ArrayList;

public class ValueSymbol extends Symbol {
    private final int dimension;
    private final ArrayList<Integer> valueList;
    private final ConstExp length;

    public ValueSymbol(SymbolType type, String name, int lineNumber, ConstExp length) {
        super(type, name, lineNumber);
        valueList = new ArrayList<>();

        if (type.equals(SymbolType.CONST_INT) || type.equals(SymbolType.STATIC_INT) || type.equals(SymbolType.INT)) {
            dimension = 0;
        } else {
            dimension = 1;
        }

        this.length = length;
    }

    // 注意：该方法不总是被调用，即 this.valueList 有可能为空（VarDef）
    public void setValue(ArrayList<Integer> valueList) {
        if (valueList != null) {
            this.valueList.addAll(valueList);
        }
    }

    public ArrayList<Integer> getValueList() {
        return valueList;
    }

    public int getDimension() {
        return dimension;
    }

    public int getLength() {
        if (length == null) {
            return 0;
        }
        return length.getComputationResult();
    }

    public IrBaseType getIrBaseType() {
        if (dimension == 0) {
            return new IrBaseType(IrBaseType.TypeValue.INT32);
        } else {
            if (length == null) {
                return new IrBaseType(IrBaseType.TypeValue.POINTER, new IrBaseType(IrBaseType.TypeValue.INT32));
            } else {
                return new IrBaseType(IrBaseType.TypeValue.INT_ARRAY, length.getComputationResult());
            }
        }
    }
}
