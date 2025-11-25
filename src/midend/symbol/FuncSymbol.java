package midend.symbol;

import frontend.parser.ast.exp.Exp;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    // 函数参数类型
    private ArrayList<ValueSymbol> paramSymbolList;

    public FuncSymbol(SymbolType type, String name, int lineNumber) {
        super(type, name, lineNumber);
        paramSymbolList = new ArrayList<>();
    }

    public void setParamsType(ArrayList<ValueSymbol> paramSymbolList) {
        this.paramSymbolList = paramSymbolList;
    }

    public ArrayList<ValueSymbol> getParamSymbols() {
        return paramSymbolList;
    }

    public boolean paramsSizeEqual(ArrayList<Exp> paramList) {
        return paramList.size() == this.paramSymbolList.size();
    }

    public boolean paramsTypeEqual(ArrayList<SymbolType> paramTypeList) {
        for (int i = 0; i < paramTypeList.size(); i++) {
            SymbolType realParamType = paramTypeList.get(i);
            SymbolType formalParamType = this.paramSymbolList.get(i).getType();
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
}
