package frontend.parser.ast.val;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;

public class InitVal extends Node {
    // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
    public InitVal() {
        super(SyntaxType.INIT_VAL);
    }

    @Override
    public void parse() {
        if (isLeftBraceToken()) {
            addAndParseNode(new TokenNode());   // '{'
            if (isRightBraceToken()) {
                addAndParseNode(new TokenNode());   // '}'
            } else {
                addAndParseNode(new Exp()); // Exp
                while (isCommaToken()) {
                    addAndParseNode(new TokenNode());   // ','
                    addAndParseNode(new Exp()); // Exp
                }
                addAndParseNode(new TokenNode());   // '}'
            }
        } else {
            addAndParseNode(new Exp());
        }
    }
}
