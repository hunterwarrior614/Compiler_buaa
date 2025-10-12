package frontend.parser.ast.stmt;

import frontend.lexer.Token;
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
        Token.Type currentType = getCurrentToken().getType();
        return currentType.equals(Token.Type.CONSTTK) || currentType.equals(Token.Type.STATICTK) || currentType.equals(Token.Type.INTTK);
    }
}
