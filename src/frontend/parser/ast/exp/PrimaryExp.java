package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.val.LVal;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.val.Number;

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
}
