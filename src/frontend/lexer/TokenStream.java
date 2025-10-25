package frontend.lexer;

import java.util.ArrayList;

public class TokenStream {
    private final ArrayList<Token> tokens;
    private int readPosition;

    public TokenStream(ArrayList<Token> tokens) {
        this.tokens = tokens;
        readPosition = 0;
    }

    public void read() {
        if (readPosition >= tokens.size()) {
            return;
        }
        readPosition++;
    }

    public Token peek(int peekStep) {
        int readPosition = this.readPosition + peekStep;
        if (readPosition >= tokens.size()) {
            return new Token(TokenType.EOF, "End of file", -1);
        }
        return tokens.get(readPosition);
    }

    public int getCurPos() {
        return readPosition;
    }

    public void setReadPosition(int readPosition) {
        this.readPosition = readPosition;
    }
}
