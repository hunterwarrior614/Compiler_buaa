package frontend.parser.ast.def;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;
import frontend.parser.ast.val.InitVal;
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
        addAndParseNode(new TokenNode());   // Ident
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
        ArrayList<Node> components = getComponents();
        String symbolName = ((TokenNode) components.get(0)).getTokenValue();
        if (isStatic) {
            // StaticIntArray
            if (components.size() > 1 && components.get(1).isTypeOfToken(TokenType.LBRACK)) {
                SymbolManager.addSymbol(SymbolType.STATIC_INT_ARRAY, symbolName);
            }
            // StaticInt
            else {
                SymbolManager.addSymbol(SymbolType.STATIC_INT, symbolName);
            }
        } else {
            // IntArray
            if (components.size() > 1 && components.get(1).isTypeOfToken(TokenType.LBRACK)) {
                SymbolManager.addSymbol(SymbolType.INT_ARRAY, symbolName);
            }
            // Int
            else {
                SymbolManager.addSymbol(SymbolType.INT, symbolName);
            }
        }
    }
}
