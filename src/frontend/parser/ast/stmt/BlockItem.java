package frontend.parser.ast.stmt;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.decl.Decl;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class BlockItem extends Node {
    // BlockItem → Decl | Stmt
    private SymbolType funcType = null;

    public BlockItem() {
        super(SyntaxType.BLOCK_ITEM);
    }

    @Override
    public void parse() {
        if (isDecl()) {
            addAndParseNode(new Decl());
        } else {
            addAndParseNode(new Stmt());
        }
    }

    private boolean isDecl() {
        TokenType currentType = getCurrentToken().getType();
        return currentType.equals(TokenType.CONSTTK) || currentType.equals(TokenType.STATICTK) || currentType.equals(TokenType.INTTK);
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        for (Node node : components) {
            setFuncType(node);
            node.visit();
        }
    }

    public void setFuncType(SymbolType funcType) {
        this.funcType = funcType;
    }

    public void setFuncType(Node node) {
        if (!(node instanceof Stmt stmt)) {
            return;
        }
        stmt.setFuncType(funcType);
    }

    public boolean hasReturnValue() {
        return (getComponents().get(0) instanceof Stmt stmt) && stmt.hasReturnValue();
    }
}
