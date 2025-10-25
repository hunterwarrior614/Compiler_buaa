package midend;

import frontend.FrontEnd;
import frontend.parser.ast.Node;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolTable;

public class MidEnd {
    private static Node rootNode;

    public static void GenerateSymbolTable() {
        SymbolManager.initialize();
        rootNode = FrontEnd.getAstTree();
        rootNode.visit();
    }

    public static SymbolTable getSymbolTable() {
        return SymbolManager.getRootSymbolTable();
    }
}
