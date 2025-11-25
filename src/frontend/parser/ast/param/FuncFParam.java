package frontend.parser.ast.param;

import frontend.parser.ast.Ident;
import frontend.parser.ast.def.BType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;
import midend.symbol.ValueSymbol;

import java.util.ArrayList;

public class FuncFParam extends Node {
    // FuncFParam → BType Ident ['[' ']']
    private ValueSymbol valueSymbol;

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


        for (Node node : components) {
            if (node instanceof Ident ident) {
                symbolName = ident.getTokenValue();
                identLineNumber = ident.getLineNumber();
            }
            node.visit();
        }

        if (components.size() > 2) {
            valueSymbol = new ValueSymbol(SymbolType.INT_ARRAY, symbolName, identLineNumber, null);   // IntArray
        } else {
            valueSymbol = new ValueSymbol(SymbolType.INT, symbolName, identLineNumber, null); // Int
        }
        SymbolManager.addSymbol(valueSymbol);
    }

    public ValueSymbol getParamSymbol() {
        return valueSymbol;
    }
}
