package midend.symbol;

public class SymbolManager {
    private static SymbolTable rootSymbolTable;
    private static SymbolTable currentSymbolTable;
    private static int symbolTableIndex;

    public static void initialize() {
        symbolTableIndex = 1;
        rootSymbolTable = new SymbolTable(symbolTableIndex, null);
        currentSymbolTable = rootSymbolTable;
    }

    public static void addSymbol(SymbolType type, String name) {
        currentSymbolTable.addSymbol(type, name);
    }

    public static void createSonSymbolTable() {
        symbolTableIndex++;
        SymbolTable sonTable = new SymbolTable(symbolTableIndex, currentSymbolTable);
        currentSymbolTable.addSonTable(sonTable);
        currentSymbolTable = sonTable;
    }

    public static void goBackToParentSymbolTable() {
        symbolTableIndex--;
        currentSymbolTable = currentSymbolTable.getParent();
    }

    public static SymbolTable getRootSymbolTable() {
        return rootSymbolTable;
    }
}
