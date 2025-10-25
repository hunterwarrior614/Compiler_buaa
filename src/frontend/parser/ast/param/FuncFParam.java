package frontend.parser.ast.param;

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
    public FuncFParam() {
        super(SyntaxType.FUNC_FORMAL_PARAM);
    }

    @Override
    public void parse() {
        addAndParseNode(new BType());   // BType
        addAndParseNode(new TokenNode());   // Ident
        // ['[' ']']
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            checkRightBracket();   // ']'
        }
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        String symbolName = ((TokenNode) components.get(1)).getTokenValue();
        // IntArray
        if (components.size() > 2) {
            SymbolManager.addSymbol(SymbolType.INT_ARRAY, symbolName);
        }
        // Int
        else {
            SymbolManager.addSymbol(SymbolType.INT, symbolName);
        }
    }
}
