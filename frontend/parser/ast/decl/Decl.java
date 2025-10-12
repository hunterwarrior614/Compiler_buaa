package frontend.parser.ast.decl;

import frontend.lexer.Token;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;

public class Decl extends Node {
    //  Decl → ConstDecl | VarDecl

    public Decl() {
        super(SyntaxType.DECL);
    }

    @Override
    public void parse() {
        if (isConstDecl()) {
            addAndParseNode(new ConstDecl());
        } else {
            addAndParseNode(new VarDecl());
        }
    }

    private boolean isConstDecl() {
        return getCurrentToken().getType().equals(Token.Type.CONSTTK);
    }
}
