package frontend.parser.ast.def;

import frontend.parser.ast.Ident;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.ConstExp;
import frontend.parser.ast.val.InitVal;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;
import midend.symbol.ValueSymbol;

import java.util.ArrayList;

public class VarDef extends Node {
    // VarDef → Ident [ '[' ConstExp ']' ] [ '=' InitVal ]
    public VarDef() {
        super(SyntaxType.VAR_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new Ident());   // Ident
        // [ '[' ConstExp ']' ]
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            addAndParseNode(new ConstExp());    // ConstExp
            checkRightBracket();   // ']'
        }
        // [ '=' InitVal ]
        if (isAssignToken()) {
            addAndParseNode(new TokenNode());   // '='
            addAndParseNode(new InitVal()); // InitVal
        }
    }

    public void visit(boolean isStatic) {
        String symbolName = "";
        SymbolType symbolType;
        int identLineNumber = 0;
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

        if (dimension == 0) {
            symbolType = isStatic ? SymbolType.STATIC_INT : SymbolType.INT;
        } else {
            symbolType = isStatic ? SymbolType.STATIC_INT_ARRAY : SymbolType.INT_ARRAY;
        }
        ValueSymbol valueSymbol = new ValueSymbol(symbolType, symbolName, identLineNumber, length);
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
            if (node instanceof InitVal initVal) {
                valueList = initVal.getValueList();
            }
        }
        valueSymbol.setValue(valueList);
    }

    public String getIdentName() {
        return ((TokenNode) components.get(0)).getTokenValue();
    }

    public boolean hasInitVal() {
        for (Node node : components) {
            if (node instanceof InitVal) {
                return true;
            }
        }
        return false;
    }

    public InitVal getInitVal() {
        for (Node node : components) {
            if (node instanceof InitVal initVal) {
                return initVal;
            }
        }
        throw new RuntimeException("[ERROR] InitVal not found in LLVM IR");
    }
}
