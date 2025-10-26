package midend.symbol;

import error.Error;
import error.ErrorRecorder;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final int id;
    private final ArrayList<Symbol> symbolList;
    private final HashMap<String, Symbol> symbolMap;

    private final SymbolTable parentTable;
    private final ArrayList<SymbolTable> sonTables;

    public SymbolTable(int id, SymbolTable parentTable) {
        this.id = id;
        symbolList = new ArrayList<>();
        symbolMap = new HashMap<>();
        this.parentTable = parentTable;
        sonTables = new ArrayList<>();
    }

    public void addSymbol(Symbol symbol) {
        // 在标识符声明时，判断该标识符名称在当前作用域下是否重复。
        // 若重复则报错
        if (symbolMap.containsKey(symbol.getName())) {
            ErrorRecorder.addError(new Error(Error.Type.b, symbol.getLineNumber()));
        } else {
            symbol.setSymbolTableId(id);
            symbolList.add(symbol);
            symbolMap.put(symbol.getName(), symbol);
        }
    }

    public void deleteSymbol(String name) {
        Symbol symbol = symbolMap.get(name);
        symbolList.remove(symbol);
        symbolMap.remove(name);
    }

    public Symbol getSymbol(String name) {
        return symbolMap.get(name);
    }

    public void addSonTable(SymbolTable sonTable) {
        sonTables.add(sonTable);
    }

    public SymbolTable getParent() {
        return parentTable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 先输出符号表内容
        for (Symbol symbol : symbolList) {
            sb.append(symbol.toString()).append("\n");
        }
        // 再依次输出子符号表
        for (SymbolTable sonTable : sonTables) {
            String sonTableString = sonTable.toString();
            if (!sonTableString.isEmpty()) {
                sb.append(sonTableString).append("\n");
            }
        }
        if (!sb.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);   // 删除末尾的换行符
        }
        return sb.toString();
    }
}
