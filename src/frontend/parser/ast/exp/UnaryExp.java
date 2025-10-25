package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.param.FuncRParams;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

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
            addAndParseNode(new TokenNode());   // Ident
            addAndParseNode(new TokenNode());   // '('
            // [FuncRParams]
            if (!isRightParenToken() && isFuncRParams()) {
                addAndParseNode(new FuncRParams());
            }
            checkRightParen();   // ')'
        }
        reConstruct();
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
}
