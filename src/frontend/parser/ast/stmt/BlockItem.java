package frontend.parser.ast.stmt;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.decl.Decl;

public class BlockItem extends Node {
    // BlockItem → Decl | Stmt
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
}
