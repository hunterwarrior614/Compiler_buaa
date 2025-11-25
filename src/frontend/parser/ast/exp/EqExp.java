package frontend.parser.ast.exp;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
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
        while (getCurrentToken().getType().equals(TokenType.EQL) || getCurrentToken().getType().equals(TokenType.NEQ)) {
            addAndParseNode(new TokenNode());   // '==' | '!='
            addAndParseNode(new RelExp());  // RelExp
        }
        reConstruct();
    }

    private void reConstruct() {
        if (components.size() > 1) {
            EqExp eqExp = new EqExp();
            int length = components.size();
            eqExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, eqExp);
            eqExp.reConstruct();
        }
    }

    // LLVM IR
    public ArrayList<RelExp> getRelExps() {
        ArrayList<RelExp> relExps = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof EqExp eqExp) {
                relExps.addAll(eqExp.getRelExps());
            } else if (node instanceof RelExp relExp) {
                relExps.add(relExp);
            }
        }
        return relExps;
    }

    public ArrayList<String> getRelOps() {
        ArrayList<String> relOps = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof EqExp eqExp) {
                relOps.addAll(eqExp.getRelOps());
            } else if (node instanceof TokenNode tokenNode) {
                relOps.add(tokenNode.getTokenValue());
            }
        }
        return relOps;
    }
}
