package frontend.parser.ast.exp;

import frontend.lexer.Token;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

import java.util.ArrayList;

public class EqExp extends Node {
    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    // 改写为：EqExp → RelExp { ('==' | '!=') RelExp }
    public EqExp() {
        super(SyntaxType.EQ_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new RelExp());
        while (getCurrentToken().getType().equals(Token.Type.EQL) || getCurrentToken().getType().equals(Token.Type.NEQ)) {
            addAndParseNode(new TokenNode());   // '==' | '!='
            addAndParseNode(new RelExp());  // RelExp
        }
        reConstruct();
    }

    private void reConstruct() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            EqExp eqExp = new EqExp();
            int length = components.size();
            eqExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, eqExp);
            eqExp.reConstruct();
        }
    }
}
