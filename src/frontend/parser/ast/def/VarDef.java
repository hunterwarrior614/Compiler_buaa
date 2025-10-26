package frontend.parser.ast.def;

import frontend.parser.ast.Ident;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;
import frontend.parser.ast.val.InitVal;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class VarDef extends Node {
    // VarDef → Ident [ '[' ConstExp ']' ] [ '=' InitVal ]
    public VarDef() {
        super(SyntaxType.VAR_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new Ident());   // Ident
        // [ '[' ConstExp ']' ]
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            addAndParseNode(new ConstExp());    // ConstExp
            checkRightBracket();   // ']'
        }
        // [ '=' InitVal ]
        if (isAssignToken()) {
            addAndParseNode(new TokenNode());   // '='
            addAndParseNode(new InitVal()); // InitVal
        }
    }

    public void visit(boolean isStatic) {
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

        if (dimension == 0) {
            symbolType = isStatic ? SymbolType.STATIC_INT : SymbolType.INT;
        } else {
            symbolType = isStatic ? SymbolType.STATIC_INT_ARRAY : SymbolType.INT_ARRAY;
        }
        SymbolManager.addSymbol(new Symbol(symbolType, symbolName, identLineNumber));
    }
}
