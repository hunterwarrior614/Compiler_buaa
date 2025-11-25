package frontend.parser.ast.decl;

import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;

public class Decl extends Node {
    //  Decl → ConstDecl | VarDecl

    public Decl() {
        super(SyntaxType.DECL);
    }

    @Override
    public void parse() {
        if (isConstDeclToken()) {
            addAndParseNode(new ConstDecl());
        } else {
            addAndParseNode(new VarDecl());
        }
    }

    private boolean isConstDeclToken() {
        return getCurrentToken().getType().equals(TokenType.CONSTTK);
    }

    // LLVM IR
    public boolean isConstDecl() {
        return components.get(0) instanceof ConstDecl;
    }

    public Node getDecl() {
        return components.get(0);
    }
}
