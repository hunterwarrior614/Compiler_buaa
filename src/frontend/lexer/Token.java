package frontend.lexer;

public class Token {
    private final TokenType type;
    private final String content;
    private final int lineNumber;

    public Token(TokenType type, String content, int lineNumber) {
        this.type = type;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public TokenType getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return type + " " + content;
    }
}
