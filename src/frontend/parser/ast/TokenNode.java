package frontend.parser.ast;

import frontend.lexer.Token;
import frontend.lexer.TokenType;

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

    public void setToken(Token token) {
        this.token = token;
    }

    public TokenType getTokenType() {
        return token.getType();
    }

    public String getTokenValue() {
        return token.getContent();
    }

    public int getLineNumber() {
        return token.getLineNumber();
    }

    @Override
    public String toString() {
        return token.toString();
    }
}
