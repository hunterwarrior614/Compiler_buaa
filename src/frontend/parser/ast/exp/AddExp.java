package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.SymbolType;

public class AddExp extends ExpNode {
    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    // 改写为 AddExp → MulExp { ('+' | '-') MulExp }
    public AddExp() {
        super(SyntaxType.ADD_EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new MulExp());
        while (isAddOrMinusToken()) {
            addAndParseNode(new TokenNode());   // '+' | '-'
            addAndParseNode(new MulExp());
        }
        reConstruct();  // 重建语法树
    }

    private boolean isAddOrMinusToken() {
        TokenType curTokenType = getCurrentToken().getType();
        return curTokenType.equals(TokenType.PLUS) || curTokenType.equals(TokenType.MINU);
    }

    private void reConstruct() {
        if (components.size() > 1) {
            AddExp addExp = new AddExp();
            int length = components.size();
            // 将当前 AddExp 的除去末尾 +/- MulExp 的所有结点移到下一层的 AddExp 中
            addExp.addNodes(components.subList(0, length - 2));
            components.subList(0, length - 2).clear();
            // 将下一层的 AddExp 添加到当前 AddExp 的子节点中
            components.add(0, addExp);
            // 递归重建
            addExp.reConstruct();
        }
    }

    public SymbolType getSymbolType() {
        if (components.size() > 1) {
            return SymbolType.VAR;
        } else {
            return ((MulExp) components.get(0)).getSymbolType();
        }
    }

    // LLVM IR
    public boolean canCompute() {
        boolean canCompute = true;
        if (this.canCompute == -1) {
            for (Node node : components) {
                if ((node instanceof AddExp addExp && !addExp.canCompute()) ||
                        (node instanceof MulExp mulExp && !mulExp.canCompute())) {
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
            computationResult = ((MulExp) components.get(0)).getComputationResult();
        } else {
            int addResult = ((AddExp) components.get(0)).getComputationResult();
            int mulResult = ((MulExp) components.get(2)).getComputationResult();
            if (components.get(1).isTypeOfToken(TokenType.PLUS)) {
                computationResult = addResult + mulResult;
            } else {
                computationResult = addResult - mulResult;
            }
        }
        return computationResult;
    }
}
