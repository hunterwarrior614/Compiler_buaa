package midend.symbol;

import midend.llvm.value.IrValue;

public class Symbol {
    protected final SymbolType type;
    protected final String name;
    protected final int lineNumber;
    protected int symbolTableId;

    protected IrValue irValue;

    public Symbol(SymbolType type, String name, int lineNumber) {
        this.type = type;
        this.name = name;
        this.lineNumber = lineNumber;
    }

    public void setSymbolTableId(int id) {
        symbolTableId = id;
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

    @Override
    public String toString() {
        if (type.equals(SymbolType.SYS_FUNC)) {
            return "";
        } else {
            return symbolTableId + " " + name + " " + type;
        }
    }

    // LLVM IR
    public void setIrValue(IrValue irValue) {
        this.irValue = irValue;
    }

    public IrValue getIrValue() {
        return irValue;
    }
}
