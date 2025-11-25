package frontend.parser.ast.stmt;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.decl.Decl;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class BlockItem extends Node {
    // BlockItem → Decl | Stmt


    public BlockItem() {
        super(SyntaxType.BLOCK_ITEM);
    }

    @Override
    public void parse() {
        if (isDeclToken()) {
            addAndParseNode(new Decl());
        } else {
            addAndParseNode(new Stmt());
        }
    }

    private boolean isDeclToken() {
        TokenType currentType = getCurrentToken().getType();
        return currentType.equals(TokenType.CONSTTK) || currentType.equals(TokenType.STATICTK) || currentType.equals(TokenType.INTTK);
    }

    // LLVM IR
    public boolean isDecl() {
        return components.get(0) instanceof Decl;
    }

    public Decl getDecl() {
        return (Decl) components.get(0);
    }

    public Stmt getStmt() {
        return (Stmt) components.get(0);
    }
}
