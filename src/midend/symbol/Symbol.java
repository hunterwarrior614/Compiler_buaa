package midend.symbol;

public class Symbol {
    private final int symbolTableId;
    private final SymbolType type;
    private final String name;

    public Symbol(int symbolTableId, SymbolType type, String name) {
        this.symbolTableId = symbolTableId;
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return symbolTableId + " " + name + " " + type;
    }
}
