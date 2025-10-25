package frontend.parser.ast.exp;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;

public class Exp extends Node {
    // Exp → AddExp
    public Exp() {
        super(SyntaxType.EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new AddExp());
    }
}
