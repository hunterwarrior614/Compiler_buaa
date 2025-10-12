package frontend.parser.ast;

import frontend.lexer.Token;

public class TokenNode extends Node {
    private Token token;

    public TokenNode() {
        super(SyntaxType.TOKEN);
    }

    @Override
    public void parse() {
        token = getCurrentToken();
        next();
    }

    @Override
    public String toString() {
        return token.toString() + "\n";
    }
}
