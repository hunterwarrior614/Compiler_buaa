package midend.symbol;

public class SymbolManager {
    private static SymbolTable rootSymbolTable;
    private static SymbolTable currentSymbolTable;
    private static int symbolTableIndex;

    private static int forDepth = 0;
    private static SymbolType currentFuncType = null;
    private static int depthInFunc = 0;
    private static boolean hasReturn = false;

    public static void initialize() {
        symbolTableIndex = 1;
        rootSymbolTable = new SymbolTable(symbolTableIndex, null);
        currentSymbolTable = rootSymbolTable;
        addSystemSymbol();  // 将自带函数、变量加入符号表
    }

    public static void addSymbol(Symbol symbol) {
        currentSymbolTable.addSymbol(symbol);
    }

    public static Symbol getSymbol(String name, boolean needIrValue) {
        SymbolTable symbolTable = currentSymbolTable;
        while (symbolTable != null) {
            // 在当前作用域符号表内查询是否有符号记录，有则返回其信息
            Symbol symbol = symbolTable.getSymbol(name);
            if (symbol != null && (!needIrValue || symbol.getIrValue() != null)) {
                return symbol;
            }
            // 否则，利用当前作用域符号表的 `fatherTable` 引用，访问外层作用域符号表
            symbolTable = symbolTable.getParent();
        }
        return null;
    }

    public static void createSonSymbolTable() {
        symbolTableIndex++;
        SymbolTable sonTable = new SymbolTable(symbolTableIndex, currentSymbolTable);
        currentSymbolTable.addSonTable(sonTable);
        currentSymbolTable = sonTable;
    }

    public static void goBackToParentSymbolTable() {
        currentSymbolTable = currentSymbolTable.getParent();
    }

    public static SymbolTable getRootSymbolTable() {
        return rootSymbolTable;
    }

    public static void addSystemSymbol() {
        rootSymbolTable.addSymbol(new FuncSymbol(SymbolType.SYS_FUNC, "printf", 0));
        rootSymbolTable.addSymbol(new FuncSymbol(SymbolType.SYS_FUNC, "getint", 0));
    }

    public static void deleteSystemSymbol() {
        rootSymbolTable.deleteSymbol("printf");
        rootSymbolTable.deleteSymbol("getint");
    }

    /* for循环块处理 */

    public static void goIntoLoopBlock() {
        forDepth++;
    }

    public static void goOutOfLoopBlock() {
        forDepth--;
    }

    public static boolean inLoopBlock() {
        return forDepth > 0;
    }

    /* 函数体处理 */
    public static void goIntoFuncBlock(SymbolType funcType) {
        currentFuncType = funcType;
    }

    public static void goOutOfFuncBlock() {
        currentFuncType = null;
        hasReturn = false;
        depthInFunc = 0;
    }

    public static void setHasReturn() {
        hasReturn = true;
    }

    public static void goIntoBlock() {
        if (currentFuncType != null) {
            depthInFunc++;
        }
    }

    public static void goOutOfBlock() {
        if (currentFuncType != null) {
            depthInFunc--;
            hasReturn = false;
        }
    }

    public static boolean inInnerBlock() {
        return depthInFunc > 0;
    }

    public static SymbolType getCurrentFuncType() {
        return currentFuncType;
    }

    public static boolean hasReturn() {
        return hasReturn;
    }

    // LLVM IR
    public static void goToSonSymbolTable() {
        currentSymbolTable = currentSymbolTable.getNextSonTable();
    }

    public static boolean isGlobal() {
        return currentSymbolTable == rootSymbolTable;
    }
}
