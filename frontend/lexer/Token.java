package frontend.lexer;

public record Token(frontend.lexer.Token.Type type, String content) {
    public enum Type {
        IDENFR, INTCON, STRCON, CONSTTK, INTTK, STATICTK, BREAKTK, CONTINUETK, IFTK, MAINTK,
        ELSETK, NOT, AND, OR, FORTK, RETURNTK, VOIDTK, PLUS, MINU, PRINTFTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
        SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE, ASSIGN
    }
}