package frontend.parser.ast.exp;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

import java.util.ArrayList;

public class LOrExp extends Node {
    // LOrExp → LAndExp | LOrExp '||' LAndExp
    // 改写为：LOrExp → LAndExp { '||' LAndExp }
    public LOrExp() {
        super(SyntaxType.LOR_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new LAndExp()); // LAndExp
        while (getCurrentToken().getType().equals(TokenType.OR)) {
            addAndParseNode(new TokenNode());   // '||'
            addAndParseNode(new LAndExp()); // LAndExp
        }
        reConstruct();
    }

    private void reConstruct() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            LOrExp lorExp = new LOrExp();
            int length = components.size();
            lorExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, lorExp);
            lorExp.reConstruct();
        }
    }
}
