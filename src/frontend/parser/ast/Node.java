package frontend.parser.ast;

import error.Error;
import error.ErrorRecorder;
import frontend.lexer.Token;
import frontend.lexer.TokenStream;
import frontend.lexer.TokenType;

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

    protected void reset(int tokenStreamPos, int errorCount) {
        tokenStream.setReadPosition(tokenStreamPos);
        currentToken = tokenStream.peek(0);

        ErrorRecorder.resetErrors(errorCount);
    }

    /* 语法错误检查 */

    protected void checkSemicolon() {
        if (!isSemicolonToken()) {
            ErrorRecorder.addError(new Error(Error.Type.i, peekToken(-1).getLineNumber()));
            // 加上分号，便于后续语义分析
            TokenNode semicolon = new TokenNode();
            semicolon.setToken(new Token(TokenType.SEMICN, ";", peekToken(-1).getLineNumber()));
            components.add(semicolon);
        } else {
            addAndParseNode(new TokenNode());
        }
    }

    protected void checkRightParen() {
        if (!isRightParenToken()) {
            ErrorRecorder.addError(new Error(Error.Type.j, peekToken(-1).getLineNumber()));
            // 加上右圆括号，便于后续语义分析
            TokenNode rightParen = new TokenNode();
            rightParen.setToken(new Token(TokenType.RPARENT, ")", peekToken(-1).getLineNumber()));
            components.add(rightParen);
        } else {
            addAndParseNode(new TokenNode());
        }
    }

    protected void checkRightBracket() {
        if (!isRightBracketToken()) {
            ErrorRecorder.addError(new Error(Error.Type.k, peekToken(-1).getLineNumber()));
            // 加上右方括号，便于后续语义分析
            TokenNode rightBracket = new TokenNode();
            rightBracket.setToken(new Token(TokenType.RBRACK, "]", peekToken(-1).getLineNumber()));
            components.add(rightBracket);
        } else {
            addAndParseNode(new TokenNode());
        }
    }

    /* Token 判别方法 */

    protected boolean isStaticToken() {
        return getCurrentToken().getType().equals(TokenType.STATICTK);
    }

    protected boolean isCommaToken() {
        return getCurrentToken().getType().equals(TokenType.COMMA);
    }

    protected boolean isLeftParenToken() {
        return getCurrentToken().getType().equals(TokenType.LPARENT);
    }

    protected boolean isRightParenToken() {
        return getCurrentToken().getType().equals(TokenType.RPARENT);
    }

    protected boolean isLeftBraceToken() {
        return getCurrentToken().getType().equals(TokenType.LBRACE);
    }

    protected boolean isRightBraceToken() {
        return getCurrentToken().getType().equals(TokenType.RBRACE);
    }

    protected boolean isLeftBracketToken() {
        return getCurrentToken().getType().equals(TokenType.LBRACK);
    }

    protected boolean isRightBracketToken() {
        return getCurrentToken().getType().equals(TokenType.RBRACK);
    }

    protected boolean isSemicolonToken() {
        return getCurrentToken().getType().equals(TokenType.SEMICN);
    }

    protected boolean isAssignToken() {
        return getCurrentToken().getType().equals(TokenType.ASSIGN);
    }

    protected boolean needPrint() {
        return !(type.equals(SyntaxType.BLOCK_ITEM) || type.equals(SyntaxType.DECL) || type.equals(SyntaxType.BTYPE));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node node : components) {
            sb.append(node.toString()).append("\n");
        }
        if (needPrint()) {
            sb.append("<").append(type).append(">");
        } else {
            sb.deleteCharAt(sb.length() - 1);   // 删除多余的换行符
        }
        return sb.toString();
    }

    public void visit() {
        for (Node node : components) {
            node.visit();
        }
    }

    public boolean isTypeOfToken(TokenType tokenType) {
        return this instanceof TokenNode && ((TokenNode) this).getTokenType().equals(tokenType);
    }
}
