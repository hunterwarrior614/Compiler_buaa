package frontend.parser.ast;

import frontend.error.Error;
import frontend.error.ErrorRecorder;
import frontend.lexer.Token;
import frontend.lexer.TokenStream;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    private final ArrayList<Node> components;
    private final SyntaxType type;
    private static Token currentToken;

    private static TokenStream tokenStream;

    public Node(SyntaxType type) {
        components = new ArrayList<>();
        this.type = type;
        currentToken = tokenStream.peek(0);
    }

    public static void initialize(TokenStream tokenStream) {
        Node.tokenStream = tokenStream;
    }

    public abstract void parse();

    protected void next() {
        tokenStream.read();
        currentToken = tokenStream.peek(0);
    }

    protected Token peekToken(int peekStep) {
        return tokenStream.peek(peekStep);
    }

    protected Token getCurrentToken() {
        return currentToken;
    }

    protected void addAndParseNode(Node node) {
        components.add(node);
        node.parse();
    }

    protected void addNodes(List<Node> nodes) {
        components.addAll(nodes);
    }

    protected ArrayList<Node> getComponents() {
        return components;
    }

    protected int getCurTokenPos() {
        return tokenStream.getCurPos();
    }

    protected void setTokenStreamPos(int tokenStreamPos) {
        tokenStream.setReadPosition(tokenStreamPos);
        currentToken = tokenStream.peek(0);
    }

    /* 语法错误检查 */

    protected void checkSemicolon() {
        if (!isSemicolonToken()) {
            ErrorRecorder.addError(new Error(Error.Type.i, peekToken(-1).getLineNumber()));
        } else {
            addAndParseNode(new TokenNode());
        }
    }

    protected void checkRightParen() {
        if (!isRightParenToken()) {
            ErrorRecorder.addError(new Error(Error.Type.j, peekToken(-1).getLineNumber()));
        } else {
            addAndParseNode(new TokenNode());
        }
    }

    protected void checkRightBracket() {
        if (!isRightBracketToken()) {
            ErrorRecorder.addError(new Error(Error.Type.k, peekToken(-1).getLineNumber()));
        } else {
            addAndParseNode(new TokenNode());
        }
    }

    /* Token 判别方法 */

    protected boolean isStaticToken() {
        return getCurrentToken().getType().equals(Token.Type.STATICTK);
    }

    protected boolean isCommaToken() {
        return getCurrentToken().getType().equals(Token.Type.COMMA);
    }

    protected boolean isLeftParenToken() {
        return getCurrentToken().getType().equals(Token.Type.LPARENT);
    }

    protected boolean isRightParenToken() {
        return getCurrentToken().getType().equals(Token.Type.RPARENT);
    }

    protected boolean isLeftBraceToken() {
        return getCurrentToken().getType().equals(Token.Type.LBRACE);
    }

    protected boolean isRightBraceToken() {
        return getCurrentToken().getType().equals(Token.Type.RBRACE);
    }

    protected boolean isLeftBracketToken() {
        return getCurrentToken().getType().equals(Token.Type.LBRACK);
    }

    protected boolean isRightBracketToken() {
        return getCurrentToken().getType().equals(Token.Type.RBRACK);
    }

    protected boolean isSemicolonToken() {
        return getCurrentToken().getType().equals(Token.Type.SEMICN);
    }

    protected boolean isAssignToken() {
        return getCurrentToken().getType().equals(Token.Type.ASSIGN);
    }

    protected boolean needPrint() {
        return !(type.equals(SyntaxType.BLOCK_ITEM) || type.equals(SyntaxType.DECL) || type.equals(SyntaxType.BTYPE));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node node : components) {
            sb.append(node.toString());
        }
        if (needPrint()) {
            sb.append("<").append(type).append(">").append("\n");
        }
        return sb.toString();
    }
}
