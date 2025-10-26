package frontend.parser.ast.param;

import frontend.parser.ast.Ident;
import frontend.parser.ast.def.BType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class FuncFParam extends Node {
    // FuncFParam → BType Ident ['[' ']']
    private SymbolType paramType;

    public FuncFParam() {
        super(SyntaxType.FUNC_FORMAL_PARAM);
    }

    @Override
    public void parse() {
        addAndParseNode(new BType());   // BType
        addAndParseNode(new Ident());   // Ident
        // ['[' ']']
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            checkRightBracket();   // ']'
        }
    }

    @Override
    public void visit() {
        String symbolName = "";
        int identLineNumber = 0;

        ArrayList<Node> components = getComponents();
        for (Node node : components) {
            if (node instanceof Ident ident) {
                symbolName = ident.getTokenValue();
                identLineNumber = ident.getLineNumber();
            }
            node.visit();
        }

        // IntArray
        if (components.size() > 2) {
            SymbolManager.addSymbol(new Symbol(SymbolType.INT_ARRAY, symbolName, identLineNumber));
            paramType = SymbolType.INT_ARRAY;
        }
        // Int
        else {
            SymbolManager.addSymbol(new Symbol(SymbolType.INT, symbolName, identLineNumber));
            paramType = SymbolType.INT;
        }
    }

    public SymbolType getParamType() {
        return paramType;
    }
}
