package frontend.parser.ast.stmt;

import frontend.lexer.Token;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.val.LVal;

public class Stmt extends Node {
    /*  Stmt → LVal '=' Exp ';'
             | [Exp] ';'
             | Block
             | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
             | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
             | 'break' ';'
             | 'continue' ';'
             | 'return' [Exp] ';'
             | 'printf''('StringConst {','Exp}')'';'
    */
    public Stmt() {
        super(SyntaxType.STATEMENT);
    }

    @Override
    public void parse() {
        // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
        if (getCurrentToken().getType().equals(Token.Type.IFTK)) {
            addAndParseNode(new TokenNode());   // 'if'
            addAndParseNode(new TokenNode());   // '('
            addAndParseNode(new Cond());    // Cond
            checkRightParen();   // ')'
            addAndParseNode(new Stmt());    // Stmt
            if (getCurrentToken().getType().equals(Token.Type.ELSETK)) {
                addAndParseNode(new TokenNode());   // 'else'
                addAndParseNode(new Stmt());    // Stmt
            }
        }
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        else if (getCurrentToken().getType().equals(Token.Type.FORTK)) {
            addAndParseNode(new TokenNode());   // 'for'
            addAndParseNode(new TokenNode());   // '('
            // [ForStmt]
            if (!isSemicolonToken()) {
                addAndParseNode(new ForStmt());
            }
            addAndParseNode(new TokenNode());   // ';'
            // [Cond]
            if (!isSemicolonToken()) {
                addAndParseNode(new Cond());
            }
            addAndParseNode(new TokenNode());   // ';'
            // [ForStmt]
            if (!isRightParenToken() && getCurrentToken().getType().equals(Token.Type.IDENFR)) {
                addAndParseNode(new ForStmt());
            }
            checkRightParen();   // ')'
            addAndParseNode(new Stmt());    // Stmt
        }
        // 'break' ';'
        else if (getCurrentToken().getType().equals(Token.Type.BREAKTK)) {
            addAndParseNode(new TokenNode());   // 'break'
            checkSemicolon();   // ;
        }
        // 'continue' ';'
        else if (getCurrentToken().getType().equals(Token.Type.CONTINUETK)) {
            addAndParseNode(new TokenNode());   // 'continue'
            checkSemicolon();   // ';'
        }
        // 'return' [Exp] ';'
        else if (getCurrentToken().getType().equals(Token.Type.RETURNTK)) {
            addAndParseNode(new TokenNode());   // 'return'
            // [Exp]
            if (!isSemicolonToken() && isExp()) {
                addAndParseNode(new Exp());
            }
            checkSemicolon();   // ';'
        }
        // 'printf''('StringConst {','Exp}')'';'
        else if (getCurrentToken().getType().equals(Token.Type.PRINTFTK)) {
            addAndParseNode(new TokenNode());   // 'printf'
            addAndParseNode(new TokenNode());   // '('
            addAndParseNode(new TokenNode());   // StringConst
            while (isCommaToken()) {
                addAndParseNode(new TokenNode());   // ','
                addAndParseNode(new Exp()); // Exp
            }
            checkRightParen();   // ')'
            checkSemicolon();   // ';'
        }
        // Block
        else if (isLeftBraceToken()) {
            addAndParseNode(new Block());
        }
        // LVal '=' Exp ';'
        else if (isCond1()) {
            addAndParseNode(new LVal());    // LVal
            addAndParseNode(new TokenNode());   // '='
            addAndParseNode(new Exp()); // Exp
            checkSemicolon();
        }
        // [Exp] ';'
        else {
            // [Exp]
            if (!isSemicolonToken() && isExp()) {
                addAndParseNode(new Exp());
            }
            checkSemicolon();
        }
    }

    private boolean isExp() {
        Token.Type curTokenType = getCurrentToken().getType();
        return curTokenType.equals(Token.Type.LPARENT) || curTokenType.equals(Token.Type.INTCON) || curTokenType.equals(Token.Type.IDENFR)
                || curTokenType.equals(Token.Type.PLUS) || curTokenType.equals(Token.Type.MINU) || curTokenType.equals(Token.Type.NOT);
    }

    private boolean isCond1() {
        int peekStep = 0;
        boolean findAssignToken = false;
        while (!peekToken(peekStep).getType().equals(Token.Type.EOF) && !peekToken(peekStep).getType().equals(Token.Type.SEMICN)) {
            if (peekToken(peekStep).getType().equals(Token.Type.ASSIGN)) {
                findAssignToken = true;
                break;
            }
            peekStep++;
        }
        return findAssignToken;
    }
}
