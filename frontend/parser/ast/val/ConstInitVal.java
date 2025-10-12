package frontend.parser.ast.val;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;

public class ConstInitVal extends Node {
    // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
    public ConstInitVal() {
        super(SyntaxType.CONST_INIT_VAL);
    }

    @Override
    public void parse() {
        if (isLeftBraceToken()) {
            addAndParseNode(new TokenNode());   // '{'
            if (isRightBraceToken()) {
                addAndParseNode(new TokenNode());   // '}'
            } else {
                addAndParseNode(new ConstExp());    // ConstExp
                while (isCommaToken()) {
                    addAndParseNode(new TokenNode());   // ','
                    addAndParseNode(new ConstExp());    // ConstExp
                }
                addAndParseNode(new TokenNode());   // '}'
            }
        } else {
            addAndParseNode(new ConstExp());
        }
    }
}
