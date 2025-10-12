package frontend.parser.ast.def;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;
import frontend.parser.ast.val.InitVal;

public class VarDef extends Node {
    // VarDef → Ident [ '[' ConstExp ']' ] [ '=' InitVal ]
    public VarDef() {
        super(SyntaxType.VAR_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());   // Ident
        // [ '[' ConstExp ']' ]
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            addAndParseNode(new ConstExp());    // ConstExp
            checkRightBracket();   // ']'
        }
        // [ '=' InitVal ]
        if (isAssignToken()) {
            addAndParseNode(new TokenNode());   // '='
            addAndParseNode(new InitVal()); // InitVal
        }
    }
}
