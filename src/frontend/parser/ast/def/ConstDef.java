package frontend.parser.ast.def;

import frontend.parser.ast.Ident;
import frontend.parser.ast.val.ConstInitVal;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class ConstDef extends Node {
    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal

    public ConstDef() {
        super(SyntaxType.CONST_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new Ident());   // Ident
        // [ '[' ConstExp ']' ]
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());
            addAndParseNode(new ConstExp());
            checkRightBracket();
        }
        addAndParseNode(new TokenNode());   // '='
        addAndParseNode(new ConstInitVal());    // ConstInitVal
    }

    @Override
    public void visit() {
        String symbolName = "";
        SymbolType symbolType;
        int identLineNumber = 0;
        int dimension = 0;

        ArrayList<Node> components = getComponents();
        for (Node node : components) {
            if (node instanceof Ident ident) {
                symbolName = ident.getTokenValue();
                identLineNumber = ident.getLineNumber();
            }
            if (node instanceof ConstExp) {
                dimension++;
            }
            node.visit();
        }

        symbolType = dimension == 0 ? SymbolType.CONST_INT : SymbolType.CONST_INT_ARRAY;
        SymbolManager.addSymbol(new Symbol(symbolType, symbolName, identLineNumber));
    }
}
