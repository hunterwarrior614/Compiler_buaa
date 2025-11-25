package frontend.parser.ast.stmt;

import error.Error;
import error.ErrorRecorder;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.val.LVal;

import java.util.ArrayList;

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

    @Override
    public void visit() {
        for (Node node : components) {
            node.visit();
            checkModified(node);
        }
    }

    private void checkModified(Node node) {
        if (!(node instanceof LVal lVal)) {
            return;
        }
        int lValLineNumber = lVal.getLineNumber();
        // 不能改变常量的值
        if (lVal.isConstVar()) {
            ErrorRecorder.addError(new error.Error(Error.Type.h, lValLineNumber));
        }
    }

    // LLVM IR
    public ArrayList<LVal> getLVals() {
        ArrayList<LVal> lVals = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof LVal lVal) {
                lVals.add(lVal);
            }
        }
        return lVals;
    }

    public ArrayList<Exp> getExps() {
        ArrayList<Exp> exps = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof Exp exp) {
                exps.add(exp);
            }
        }
        return exps;
    }
}
