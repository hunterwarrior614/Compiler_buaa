package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.val.LVal;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.val.Number;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class PrimaryExp extends Node {
    // PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExp() {
        super(SyntaxType.PRIMARY_EXP);
    }

    @Override
    public void parse() {
        if (isLeftParenToken()) {
            addAndParseNode(new TokenNode());   // '('
            addAndParseNode(new Exp()); // Exp
            checkRightParen();   // ')'
        } else if (isIdentifier()) {
            addAndParseNode(new LVal());
        } else {
            addAndParseNode(new Number());
        }
    }


    private boolean isIdentifier() {
        return getCurrentToken().getType().equals(TokenType.IDENFR);
    }

    public SymbolType getSymbolType() {
        ArrayList<Node> components = getComponents();
        if (components.get(0) instanceof Number) {
            return SymbolType.VAR;
        } else if (components.get(0) instanceof LVal) {
            return ((LVal) components.get(0)).getSymbolType();
        } else {
            return ((Exp) components.get(1)).getSymbolType();
        }
    }
}
