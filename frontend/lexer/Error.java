package frontend.lexer;

public class Error {
    public enum Type {
        a;
    }

    private final Type type = Type.a;
    private final int lineno;

    public Error(int lineno) {
        this.lineno = lineno;
    }

    public int getLineno() {
        return lineno;
    }

    public Type getType() {
        return type;
    }
}
