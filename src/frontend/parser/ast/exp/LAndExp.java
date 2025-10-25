package frontend.parser.ast.exp;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

import java.util.ArrayList;

public class LAndExp extends Node {
    // LAndExp → EqExp | LAndExp '&&' EqExp
    // 改写为：LAndExp → EqExp { '&&' EqExp }
    public LAndExp() {
        super(SyntaxType.LAND_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new EqExp());
        while (getCurrentToken().getType().equals(TokenType.AND)) {
            addAndParseNode(new TokenNode());   // ‘&&’
            addAndParseNode(new EqExp());   // EqExp
        }
        reConstruct();
    }

    private void reConstruct() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            LAndExp lAndExp = new LAndExp();
            int length = components.size();
            lAndExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, lAndExp);
            lAndExp.reConstruct();
        }
    }
}
