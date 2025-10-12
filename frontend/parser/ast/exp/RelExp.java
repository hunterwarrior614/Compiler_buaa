package frontend.parser.ast.exp;

import frontend.lexer.Token;
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
        while (getCurrentToken().getType().equals(Token.Type.LSS) || getCurrentToken().getType().equals(Token.Type.LEQ)
                || getCurrentToken().getType().equals(Token.Type.GRE) || getCurrentToken().getType().equals(Token.Type.GEQ)) {
            addAndParseNode(new TokenNode());   // '<' | '>' | '<=' | '>='
            addAndParseNode(new AddExp());  // AddExp
        }
        reConstruct();
    }

    private void reConstruct() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            RelExp relExp = new RelExp();
            int length = components.size();
            relExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, relExp);
            relExp.reConstruct();
        }
    }
}
