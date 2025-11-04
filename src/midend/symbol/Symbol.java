package midend.symbol;

import frontend.parser.ast.exp.Exp;

import java.util.ArrayList;

public class Symbol {
    private final SymbolType type;
    private final String name;
    private final int lineNumber;
    private int symbolTableId;
    // 函数参数类型
    ArrayList<SymbolType> paramTypeList;

    public Symbol(SymbolType type, String name, int lineNumber) {
        this.type = type;
        this.name = name;
        this.lineNumber = lineNumber;
        paramTypeList = new ArrayList<>();
    }

    public void setSymbolTableId(int id) {
        symbolTableId = id;
    }

    public void setParamsType(ArrayList<SymbolType> paramTypeList) {
        this.paramTypeList = paramTypeList;
    }

    public SymbolType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean paramsSizeEqual(ArrayList<Exp> paramList) {
        return paramList.size() == this.paramTypeList.size();
    }

    public boolean paramsTypeEqual(ArrayList<SymbolType> paramTypeList) {
        for (int i = 0; i < paramTypeList.size(); i++) {
            SymbolType realParamType = paramTypeList.get(i);
            SymbolType formalParamType = this.paramTypeList.get(i);
            if (realParamType == null) {
                continue;
            }
            if (realParamType.equals(SymbolType.VAR) && (formalParamType.equals(SymbolType.INT_ARRAY)) ||
                    realParamType.equals(SymbolType.ARRAY) && (formalParamType.equals(SymbolType.INT))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (type.equals(SymbolType.SYS_FUNC)) {
            return "";
        } else {
            return symbolTableId + " " + name + " " + type;
        }
    }
}
