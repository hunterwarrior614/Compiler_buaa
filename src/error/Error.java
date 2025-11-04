package error;

public class Error {
    public enum Type {
        a,
        i, j, k,
        b, c, d, e, f, g, h, l, m,
    }

    private final Type type;
    private final int lineNumber;

    public Error(Type type, int lineno) {
        this.type = type;
        this.lineNumber = lineno;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return lineNumber + " " + type;
    }
}
