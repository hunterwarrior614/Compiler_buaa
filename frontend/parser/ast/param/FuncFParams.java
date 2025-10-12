package frontend.parser.ast.param;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class FuncFParams extends Node {
    // FuncFParams → FuncFParam { ',' FuncFParam }
    public FuncFParams() {
        super(SyntaxType.FUNC_FORMAL_PARAMS);
    }

    @Override
    public void parse() {
        addAndParseNode(new FuncFParam());  // FuncFParam
        while (isCommaToken()) {
            addAndParseNode(new TokenNode());   // ','
            addAndParseNode(new FuncFParam());  // FuncFParam
        }
    }
}
