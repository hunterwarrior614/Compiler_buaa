package frontend.parser.ast.def;

import frontend.parser.ast.Ident;
import frontend.parser.ast.val.ConstInitVal;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;
import midend.symbol.ValueSymbol;

import java.util.ArrayList;

public class ConstDef extends Node {
    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal

    public ConstDef() {
        super(SyntaxType.CONST_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new Ident());   // Ident
        // [ '[' ConstExp ']' ]
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());
            addAndParseNode(new ConstExp());
            checkRightBracket();
        }
        addAndParseNode(new TokenNode());   // '='
        addAndParseNode(new ConstInitVal());    // ConstInitVal
    }

    @Override
    public void visit() {
        String symbolName = "";
        int identLineNumber = 0;
        SymbolType valueType;
        int dimension = 0;
        ConstExp length = null;

        for (Node node : components) {
            if (node instanceof Ident ident) {
                symbolName = ident.getTokenValue();
                identLineNumber = ident.getLineNumber();
            }
            if (node instanceof ConstExp constExp) {
                length = constExp;
                dimension++;
            }
            node.visit();
        }

        valueType = dimension == 0 ? SymbolType.CONST_INT : SymbolType.CONST_INT_ARRAY;
        ValueSymbol valueSymbol = new ValueSymbol(valueType, symbolName, identLineNumber, length);
        SymbolManager.addSymbol(valueSymbol);
    }

    // LLVM IR
    public void setValueToSymbol() {
        ValueSymbol valueSymbol = (ValueSymbol) SymbolManager.getSymbol(getIdentName());
        if (valueSymbol == null) {
            throw new RuntimeException("[ERROR] Symbol not found in LLVM IR");
        }

        ArrayList<Integer> valueList = null;
        for (Node node : components) {
            if (node instanceof ConstInitVal constInitVal) {
                valueList = constInitVal.getValueList();
            }
        }
        valueSymbol.setValue(valueList);
    }

    public String getIdentName() {
        return ((TokenNode) components.get(0)).getTokenValue();
    }

    public ConstInitVal getConstInitVal() {
        for (Node node : components) {
            if (node instanceof ConstInitVal constInitVal) {
                return constInitVal;
            }
        }
        return null;
    }
}
