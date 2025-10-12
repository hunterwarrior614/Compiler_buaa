package frontend.parser.ast.val;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class Number extends Node {
    // Number → IntConst
    public Number() {
        super(SyntaxType.NUMBER);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());
    }
}
