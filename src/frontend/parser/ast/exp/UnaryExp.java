package frontend.parser.ast.exp;

import error.Error;
import error.ErrorRecorder;
import frontend.lexer.TokenType;
import frontend.parser.ast.Ident;
import frontend.parser.ast.param.FuncRParams;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.FuncSymbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class UnaryExp extends ExpNode {
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    // 改写为：UnaryExp → { UnaryOp } (PrimaryExp | Ident '(' [FuncRParams] ')')
    public UnaryExp() {
        super(SyntaxType.UNARY_EXP);
    }

    @Override
    public void parse() {
        // { UnaryOp }
        while (isUnaryOp()) {
            addAndParseNode(new UnaryOp());
        }

        if (isPrimaryExp()) {
            addAndParseNode(new PrimaryExp());
        } else {
            addAndParseNode(new Ident());   // Ident
            addAndParseNode(new TokenNode());   // '('
            // [FuncRParams]
            if (!isRightParenToken() && isFuncRParams()) {
                addAndParseNode(new FuncRParams());
            }
            checkRightParen();   // ')'
        }
        reConstruct();
    }

    @Override
    public void visit() {
        if (components.get(0) instanceof Ident) {
            FuncSymbol func = null;
            FuncRParams funcRParams = null;
            int lineNumber = 0;
            for (Node node : components) {
                node.visit();
                if (node instanceof Ident ident) {
                    String identName = ident.getTokenValue();
                    lineNumber = ident.getLineNumber();
                    FuncSymbol symbol = (FuncSymbol) SymbolManager.getSymbol(identName, false);
                    if (symbol == null) {
                        ErrorRecorder.addError(new Error(Error.Type.c, lineNumber));
                    } else {
                        func = symbol;
                    }
                }
                if (node instanceof FuncRParams) {
                    funcRParams = (FuncRParams) node;
                }
                checkFuncParams(node, func, funcRParams, lineNumber);
            }
        } else {
            super.visit();
        }
    }

    private boolean isUnaryOp() {
        TokenType curTokenType = getCurrentToken().getType();
        return curTokenType.equals(TokenType.PLUS) || curTokenType.equals(TokenType.MINU) || curTokenType.equals(TokenType.NOT);
    }

    private boolean isPrimaryExp() {
        return !(getCurrentToken().getType().equals(TokenType.IDENFR) && peekToken(1).getType().equals(TokenType.LPARENT));
    }

    private boolean isFuncRParams() {
        TokenType curTokenType = getCurrentToken().getType();
        return curTokenType.equals(TokenType.LPARENT) || curTokenType.equals(TokenType.INTCON) || curTokenType.equals(TokenType.IDENFR)
                || curTokenType.equals(TokenType.PLUS) || curTokenType.equals(TokenType.MINU) || curTokenType.equals(TokenType.NOT);
    }

    private void reConstruct() {
        if (components.get(0) instanceof UnaryOp) {
            UnaryExp unaryExp = new UnaryExp();
            unaryExp.addNodes(components.subList(1, components.size()));
            components.subList(1, components.size()).clear();
            components.add(unaryExp);
            unaryExp.reConstruct();
        }
    }


    private void checkFuncParams(Node node, FuncSymbol func, FuncRParams funcRParams, int lineNumber) {
        if (func == null || !(node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.RPARENT))) {
            return;
        }

        // 函数参数个数是否匹配
        ArrayList<Exp> realParams = funcRParams == null ? new ArrayList<>() : funcRParams.getParamList();
        if (!func.paramsSizeEqual(realParams)) {
            ErrorRecorder.addError(new Error(Error.Type.d, lineNumber));
            return;
        }
        // 函数参数类型是否匹配
        ArrayList<SymbolType> realParamsType = new ArrayList<>();
        for (Exp exp : realParams) {
            realParamsType.add(exp.getSymbolType());
        }
        if (!func.paramsTypeEqual(realParamsType)) {
            ErrorRecorder.addError(new Error(Error.Type.e, lineNumber));
        }
    }

    public SymbolType getSymbolType() {
        if (components.size() > 1) {
            return SymbolType.VAR;
        } else {
            return ((PrimaryExp) components.get(0)).getSymbolType();
        }
    }

    /* LLVM IR */
    public boolean canCompute() {
        boolean canCompute;
        if (this.canCompute == -1) {
            // PrimaryExp
            if (components.size() == 1) {
                canCompute = ((PrimaryExp) components.get(0)).canCompute();
                this.canCompute = canCompute ? 1 : 0;
                return canCompute;
            }
            // UnaryOp UnaryExp
            else if (components.size() == 2) {
                canCompute = ((UnaryExp) components.get(1)).canCompute();
                this.canCompute = canCompute ? 1 : 0;
                return canCompute;
            }
            // Ident '(' [FuncRParams] ')'
            else {
                this.canCompute = 0;
                return false;
            }
        } else {
            return this.canCompute == 1;
        }
    }

    public int getComputationResult() {
        if (!canCompute()) {
            throw new RuntimeException("[ERROR] Can't compute result");
        }
        if (validResult) {
            return computationResult;
        }

        validResult = true;
        if (components.size() == 1) {
            // PrimaryExp
            computationResult = ((PrimaryExp) components.get(0)).getComputationResult();
        } else {
            // UnaryOp UnaryExp
            TokenType opType = ((UnaryOp) components.get(0)).getOperator();
            int unaryResult = ((UnaryExp) components.get(1)).getComputationResult();
            if (opType.equals(TokenType.MINU)) {
                unaryResult *= -1;
            }else if(opType.equals(TokenType.NOT)) {    // 千万不要忘了取反！
                unaryResult = unaryResult == 0 ? 1 : 0;
            }
            computationResult = unaryResult;
        }
        return computationResult;
    }

    // PrimaryExp
    public PrimaryExp getPrimaryExp() {
        return (PrimaryExp) components.get(0);
    }

    // UnaryOp UnaryExp
    public TokenType getUnaryOp() {
        return ((UnaryOp) components.get(0)).getOperator();
    }

    public UnaryExp getUnaryExp() {
        return (UnaryExp) components.get(1);
    }

    // Ident '(' [FuncRParams] ')'
    public String getIdentName() {
        return ((TokenNode) components.get(0)).getTokenValue();
    }

    public ArrayList<Exp> getParams() {
        ArrayList<Exp> params = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof FuncRParams funcRParams) {
                params.addAll(funcRParams.getParamList());
            }
        }
        return params;
    }
}
