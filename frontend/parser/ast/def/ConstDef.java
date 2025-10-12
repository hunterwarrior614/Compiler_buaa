package frontend.parser.ast.def;

import frontend.parser.ast.val.ConstInitVal;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;

public class ConstDef extends Node {
    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal

    public ConstDef() {
        super(SyntaxType.CONST_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());   // Ident
        // [ '[' ConstExp ']' ]
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());
            addAndParseNode(new ConstExp());
            checkRightBracket();
        }
        addAndParseNode(new TokenNode());   // '='
        addAndParseNode(new ConstInitVal());    // ConstInitVal
    }
}
