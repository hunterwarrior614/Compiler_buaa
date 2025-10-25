package frontend.parser.ast.param;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

import java.util.ArrayList;

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

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        components.get(0).visit();  // FuncFParam
        int idx = 1;
        while (idx < components.size() - 1) {
            components.get(idx + 1).visit();
            idx += 2;
        }
    }
}
