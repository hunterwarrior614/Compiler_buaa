package frontend.parser.ast.exp;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class UnaryOp extends Node {
    // UnaryOp → '+' | '−' | '!'
    public UnaryOp() {
        super(SyntaxType.UNARY_OP);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());
    }
}
