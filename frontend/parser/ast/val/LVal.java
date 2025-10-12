package frontend.parser.ast.val;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;

public class LVal extends Node {
    // LVal → Ident ['[' Exp ']']
    public LVal() {
        super(SyntaxType.LVAL);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());   // Ident
        // ['[' Exp ']']
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            addAndParseNode(new Exp()); // Exp
            checkRightBracket();   // ']'
        }
    }
}
