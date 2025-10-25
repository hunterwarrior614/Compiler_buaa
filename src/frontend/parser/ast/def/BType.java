package frontend.parser.ast.def;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class BType extends Node {
    // BType → 'int'

    public BType() {
        super(SyntaxType.BTYPE);
    }

    @Override
    public void parse() {
        // 'int'
        addAndParseNode(new TokenNode());
    }
}
