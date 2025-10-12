package frontend.parser.ast.stmt;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.val.LVal;

public class ForStmt extends Node {
    // ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
    public ForStmt() {
        super(SyntaxType.FOR_STATEMENT);
    }

    @Override
    public void parse() {
        addAndParseNode(new LVal());    // LVal
        addAndParseNode(new TokenNode());   // '='
        addAndParseNode(new Exp()); // Exp
        while (isCommaToken()) {
            addAndParseNode(new TokenNode());   // ','
            addAndParseNode(new LVal());    // LVal
            addAndParseNode(new TokenNode());   // '='
            addAndParseNode(new Exp()); // Exp
        }
    }
}
