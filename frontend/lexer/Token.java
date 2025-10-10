package frontend.lexer;

public class Token {
    public enum Type {
        IDENFR, INTCON, STRCON, CONSTTK, INTTK, STATICTK, BREAKTK, CONTINUETK, IFTK, MAINTK,
        ELSETK, NOT, AND, OR, FORTK, RETURNTK, VOIDTK, PLUS, MINU, PRINTFTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
        SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE, ASSIGN
    }

    private final Type type;
    private final String content;

    public Token(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public String toString() {
        return type + " " + content;
    }
}
