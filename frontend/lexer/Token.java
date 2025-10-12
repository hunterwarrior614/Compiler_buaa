package frontend.lexer;

public class Token {
    public enum Type {
        IDENFR, INTCON, STRCON, CONSTTK, INTTK, STATICTK, BREAKTK, CONTINUETK, IFTK, MAINTK,
        ELSETK, NOT, AND, OR, FORTK, RETURNTK, VOIDTK, PLUS, MINU, PRINTFTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
        SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE, ASSIGN, EOF
    }

    private final Type type;
    private final String content;
    private final int lineNumber;

    public Token(Type type, String content, int lineNumber) {
        this.type = type;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public Type getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return type + " " + content;
    }
}
