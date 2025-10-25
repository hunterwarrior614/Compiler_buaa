package midend.symbol;

import java.util.ArrayList;

public class SymbolTable {
    private final int id;
    private final ArrayList<Symbol> symbols;

    private final SymbolTable parentTable;
    private final ArrayList<SymbolTable> sonTables;

    public SymbolTable(int id, SymbolTable parentTable) {
        this.id = id;
        symbols = new ArrayList<>();
        this.parentTable = parentTable;
        sonTables = new ArrayList<>();
    }

    public void addSymbol(SymbolType type, String name) {
        symbols.add(new Symbol(id, type, name));
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
        for (Symbol symbol : symbols) {
            sb.append(symbol.toString()).append("\n");
        }
        // 再依次输出子符号表
        for (SymbolTable sonTable : sonTables) {
            sb.append(sonTable.toString()).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);   // 删除末尾的换行符
        return sb.toString();
    }
}
