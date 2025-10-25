package error;

public class Error {
    public enum Type {
        a, i, j, k
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

    @Override
    public String toString() {
        return lineNumber + " " + type;
    }
}
