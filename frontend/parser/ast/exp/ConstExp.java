package frontend.parser.ast.exp;


import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;

public class ConstExp extends Node {
    // ConstExp → AddExp
    public ConstExp() {
        super(SyntaxType.CONST_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new AddExp());
    }
}
