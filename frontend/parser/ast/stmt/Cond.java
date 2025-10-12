package frontend.parser.ast.stmt;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.exp.LOrExp;

public class Cond extends Node {
    // Cond → LOrExp
    public Cond() {
        super(SyntaxType.CONDITION);
    }

    @Override
    public void parse() {
        addAndParseNode(new LOrExp());
    }
}
