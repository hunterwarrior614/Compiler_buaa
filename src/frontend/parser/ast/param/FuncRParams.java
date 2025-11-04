package frontend.parser.ast.param;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;

import java.util.ArrayList;

public class FuncRParams extends Node {
    // FuncRParams → Exp { ',' Exp }
    private final ArrayList<Exp> paramList;

    public FuncRParams() {
        super(SyntaxType.FUNC_REAL_PARAMS);
        this.paramList = new ArrayList<>();
    }

    @Override
    public void parse() {
        Exp exp = new Exp();
        addAndParseNode(exp);
        paramList.add(exp);
        while (isCommaToken()) {
            addAndParseNode(new TokenNode());   // ','
            exp = new Exp();
            addAndParseNode(exp);
            paramList.add(exp);
        }
    }

    public ArrayList<Exp> getParamList() {
        return paramList;
    }
}
