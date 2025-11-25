package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.SymbolType;

public class MulExp extends ExpNode {
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
        if (components.size() > 1) {
            return SymbolType.VAR;
        } else {
            return ((UnaryExp) components.get(0)).getSymbolType();
        }
    }

    // LLVM IR
    public boolean canCompute() {
        boolean canCompute = true;
        if (this.canCompute == -1) {
            for (Node node : components) {
                if ((node instanceof MulExp mulExp && !mulExp.canCompute()) ||
                        (node instanceof UnaryExp unaryExp && !unaryExp.canCompute())) {
                    canCompute = false;
                }
            }
            this.canCompute = canCompute ? 1 : 0;
            return canCompute;
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
            computationResult = ((UnaryExp) components.get(0)).getComputationResult();
        } else {
            int mulResult = ((MulExp) components.get(0)).getComputationResult();
            int unaryResult = ((UnaryExp) components.get(2)).getComputationResult();
            if (components.get(1).isTypeOfToken(TokenType.MULT)) {
                computationResult = mulResult * unaryResult;
            } else if (components.get(1).isTypeOfToken(TokenType.DIV)) {
                computationResult = mulResult / unaryResult;    // TODO:除零？
            } else {
                computationResult = mulResult % unaryResult;
            }
        }
        return computationResult;
    }
}
