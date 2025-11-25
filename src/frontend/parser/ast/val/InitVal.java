package frontend.parser.ast.val;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;

import java.util.ArrayList;

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

    // LLVM IR
    public ArrayList<Exp> getExpList() {
        ArrayList<Exp> expList = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof Exp exp) {
                expList.add(exp);
            }
        }
        return expList;
    }

    public ArrayList<Integer> getValueList() {
        ArrayList<Integer> valueList = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof Exp exp) {
                valueList.add(exp.getComputationResult());
            }
        }
        return valueList;
    }
}
