package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class MulExp extends Node {
    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 改写为： MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }
    public MulExp() {
        super(SyntaxType.MUL_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new UnaryExp());
        while (isDivOrMulOrModToken()) {
            addAndParseNode(new TokenNode());
            addAndParseNode(new UnaryExp());
        }
        reConstruct();
    }

    private boolean isDivOrMulOrModToken() {
        TokenType curTokenType = getCurrentToken().getType();
        return curTokenType.equals(TokenType.MULT) || curTokenType.equals(TokenType.MOD) || curTokenType.equals(TokenType.DIV);
    }

    private void reConstruct() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            MulExp mulExp = new MulExp();
            int length = components.size();
            mulExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            components.add(0, mulExp);

            mulExp.reConstruct();
        }
    }

    public SymbolType getSymbolType() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            return SymbolType.VAR;
        } else {
            return ((UnaryExp) components.get(0)).getSymbolType();
        }
    }
}
