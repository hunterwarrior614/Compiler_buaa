package frontend.parser.ast.exp;

import error.Error;
import error.ErrorRecorder;
import frontend.lexer.TokenType;
import frontend.parser.ast.Ident;
import frontend.parser.ast.param.FuncRParams;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class UnaryExp extends Node {
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
        ArrayList<Node> components = getComponents();
        if (components.get(0) instanceof Ident) {
            Symbol func = null;
            FuncRParams funcRParams = null;
            int lineNumber = 0;
            for (Node node : components) {
                node.visit();
                if (node instanceof Ident ident) {
                    String identName = ident.getTokenValue();
                    lineNumber = ident.getLineNumber();
                    Symbol symbol = SymbolManager.getSymbol(identName);
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
        ArrayList<Node> components = getComponents();
        if (components.get(0) instanceof UnaryOp) {
            UnaryExp unaryExp = new UnaryExp();
            unaryExp.addNodes(components.subList(1, components.size()));
            components.subList(1, components.size()).clear();
            components.add(unaryExp);
            unaryExp.reConstruct();
        }
    }


    private void checkFuncParams(Node node, Symbol func, FuncRParams funcRParams, int lineNumber) {
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
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            return SymbolType.VAR;
        } else {
            return ((PrimaryExp) components.get(0)).getSymbolType();
        }
    }
}
