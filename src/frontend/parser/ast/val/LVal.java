package frontend.parser.ast.val;

import error.Error;
import error.ErrorRecorder;
import frontend.parser.ast.Ident;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class LVal extends Node {
    // LVal → Ident ['[' Exp ']']
    public LVal() {
        super(SyntaxType.LVAL);
    }

    @Override
    public void parse() {
        addAndParseNode(new Ident());   // Ident
        // ['[' Exp ']']
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            addAndParseNode(new Exp()); // Exp
            checkRightBracket();   // ']'
        }
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        for (Node node : components) {
            checkUndefined(node);   // 检查未定义
            node.visit();
        }
    }

    private void checkUndefined(Node node) {
        if (!(node instanceof Ident ident)) {
            return;
        }

        String identName = ident.getTokenValue();
        if (SymbolManager.getSymbol(identName) == null) {
            ErrorRecorder.addError(new Error(Error.Type.c, ident.getLineNumber()));
        }
    }

    public SymbolType getSymbolType() {
        ArrayList<Node> components = getComponents();
        if (components.size() > 1) {
            return SymbolType.VAR;
        } else {
            Ident ident = (Ident) components.get(0);
            Symbol symbol = SymbolManager.getSymbol(ident.getTokenValue());
            if (symbol == null) {
                return null;
            }
            SymbolType symbolType = symbol.getType();
            if (symbolType.equals(SymbolType.INT_ARRAY) || symbolType.equals(SymbolType.CONST_INT_ARRAY) || symbolType.equals(SymbolType.STATIC_INT_ARRAY)) {
                return SymbolType.ARRAY;
            } else {
                return SymbolType.VAR;
            }
        }
    }

    public boolean isConstVar() {
        ArrayList<Node> components = getComponents();
        Ident ident = (Ident) components.get(0);
        Symbol symbol = SymbolManager.getSymbol(ident.getTokenValue());
        // const int:a;
        if (components.size() == 1) {
            if (symbol == null) {
                return false;
            }
            SymbolType symbolType = symbol.getType();
            return symbolType.equals(SymbolType.CONST_INT);
        }
        // const int:a[0];
        else {
            if (symbol == null) {
                return false;
            }
            SymbolType symbolType = symbol.getType();
            return symbolType.equals(SymbolType.CONST_INT_ARRAY);
        }
    }

    public int getLineNumber() {
        return ((Ident) getComponents().get(0)).getLineNumber();
    }
}
