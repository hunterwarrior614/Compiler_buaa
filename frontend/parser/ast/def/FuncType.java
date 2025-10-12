package frontend.parser.ast.def;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class FuncType extends Node {
    // FuncType → 'void' | 'int'
    public FuncType() {
        super(SyntaxType.FUNC_TYPE);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());
    }
}
