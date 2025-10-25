package frontend.parser.ast.def;

import frontend.lexer.TokenType;
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
        addAndParseNode(new TokenNode());   // Ident
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
        ArrayList<Node> components = getComponents();
        String symbolName = ((TokenNode) components.get(0)).getTokenValue();
        // ConstIntArray
        if (components.get(1).isTypeOfToken(TokenType.LBRACK)) {
            SymbolManager.addSymbol(SymbolType.CONST_INT_ARRAY, symbolName);
        }
        // ConstInt
        else {
            SymbolManager.addSymbol(SymbolType.CONST_INT, symbolName);
        }
    }
}
