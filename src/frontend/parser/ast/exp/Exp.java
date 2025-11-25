package frontend.parser.ast.exp;

import frontend.parser.ast.SyntaxType;
import midend.symbol.SymbolType;

public class Exp extends ExpNode {
    // Exp → AddExp
    public Exp() {
        super(SyntaxType.EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new AddExp());
    }

    public SymbolType getSymbolType() {
        return ((AddExp) components.get(0)).getSymbolType();
    }

    // LLVM IR
    public AddExp getAddExp() {
        return (AddExp) components.get(0);
    }

    public boolean canCompute() {
        boolean canCompute;
        if (this.canCompute == -1) {
            canCompute = ((AddExp) components.get(0)).canCompute();
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
        computationResult = ((AddExp) components.get(0)).getComputationResult();
        return computationResult;
    }
}
