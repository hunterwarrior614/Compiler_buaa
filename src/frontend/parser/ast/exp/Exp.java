package frontend.parser.ast.exp;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import midend.symbol.Symbol;
import midend.symbol.SymbolType;

public class Exp extends Node {
    // Exp → AddExp
    public Exp() {
        super(SyntaxType.EXP);
    }

    @Override
    public void parse() {
        addAndParseNode(new AddExp());
    }

    public SymbolType getSymbolType() {
        return ((AddExp) getComponents().get(0)).getSymbolType();
    }
}
