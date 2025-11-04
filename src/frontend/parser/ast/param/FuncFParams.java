package frontend.parser.ast.param;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.Symbol;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class FuncFParams extends Node {
    // FuncFParams → FuncFParam { ',' FuncFParam }
    private final ArrayList<FuncFParam> funcFParams;

    public FuncFParams() {
        super(SyntaxType.FUNC_FORMAL_PARAMS);
        this.funcFParams = new ArrayList<>();
    }

    @Override
    public void parse() {
        FuncFParam funcFParam = new FuncFParam();
        addAndParseNode(funcFParam);  // FuncFParam
        funcFParams.add(funcFParam);
        while (isCommaToken()) {
            addAndParseNode(new TokenNode());   // ','
            funcFParam = new FuncFParam();
            addAndParseNode(funcFParam);  // FuncFParam
            funcFParams.add(funcFParam);
        }
    }

    public ArrayList<SymbolType> getParamsType() {
        ArrayList<SymbolType> types = new ArrayList<>();
        for (FuncFParam param : funcFParams) {
            types.add(param.getParamType());
        }
        return types;
    }
}
