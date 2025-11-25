package frontend.parser.ast.exp;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

import java.util.ArrayList;

public class RelExp extends Node {
    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // 改写为：RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp }
    public RelExp() {
        super(SyntaxType.REL_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new AddExp());  // AddExp
        while (getCurrentToken().getType().equals(TokenType.LSS) || getCurrentToken().getType().equals(TokenType.LEQ)
                || getCurrentToken().getType().equals(TokenType.GRE) || getCurrentToken().getType().equals(TokenType.GEQ)) {
            addAndParseNode(new TokenNode());   // '<' | '>' | '<=' | '>='
            addAndParseNode(new AddExp());  // AddExp
        }
        reConstruct();
    }

    private void reConstruct() {
        if (components.size() > 1) {
            RelExp relExp = new RelExp();
            int length = components.size();
            relExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, relExp);
            relExp.reConstruct();
        }
    }

    // LLVM IR
    public ArrayList<AddExp> getAddExps() {
        ArrayList<AddExp> addExps = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof RelExp relExp) {
                addExps.addAll(relExp.getAddExps());
            } else if (node instanceof AddExp addExp) {
                addExps.add(addExp);
            }
        }
        return addExps;
    }

    public ArrayList<String> getRelOps() {
        ArrayList<String> relOps = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof RelExp relExp) {
                relOps.addAll(relExp.getRelOps());
            } else if (node instanceof TokenNode tokenNode) {
                relOps.add(tokenNode.getTokenValue());
            }
        }
        return relOps;
    }
}
