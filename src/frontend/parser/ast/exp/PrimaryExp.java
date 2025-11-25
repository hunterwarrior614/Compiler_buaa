package frontend.parser.ast.exp;

import frontend.lexer.TokenType;
import frontend.parser.ast.val.LVal;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.val.Number;
import midend.symbol.SymbolType;

public class PrimaryExp extends ExpNode {
    // PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExp() {
        super(SyntaxType.PRIMARY_EXP);
    }

    @Override
    public void parse() {
        if (isLeftParenToken()) {
            addAndParseNode(new TokenNode());   // '('
            addAndParseNode(new Exp()); // Exp
            checkRightParen();   // ')'
        } else if (isIdentifier()) {
            addAndParseNode(new LVal());
        } else {
            addAndParseNode(new Number());
        }
    }


    private boolean isIdentifier() {
        return getCurrentToken().getType().equals(TokenType.IDENFR);
    }

    public SymbolType getSymbolType() {

        if (components.get(0) instanceof Number) {
            return SymbolType.VAR;
        } else if (components.get(0) instanceof LVal) {
            return ((LVal) components.get(0)).getSymbolType();
        } else {
            return ((Exp) components.get(1)).getSymbolType();
        }
    }

    // LLVM IR
    public boolean canCompute() {
        boolean canCompute;
        if (this.canCompute == -1) {
            if (components.size() == 1) {
                // Number
                if (components.get(0) instanceof Number) {
                    this.canCompute = 1;
                    return true;
                }
                // LVal
                else {
                    canCompute = ((LVal) components.get(0)).canGetValue();
                    this.canCompute = canCompute ? 1 : 0;
                    return canCompute;
                }
            }
            // '(' Exp ')'
            else {
                canCompute = ((Exp) components.get(1)).canCompute();
                this.canCompute = canCompute ? 1 : 0;
                return canCompute;
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
        // '(' Exp ')'
        if (components.size() > 1) {
            computationResult = ((Exp) components.get(1)).getComputationResult();
        }
        // Number
        else if (components.get(0) instanceof Number number) {
            computationResult = number.getIntValue();
        }
        // LVal
        else {
            computationResult = ((LVal) components.get(0)).getValue();
        }
        return computationResult;
    }
}
