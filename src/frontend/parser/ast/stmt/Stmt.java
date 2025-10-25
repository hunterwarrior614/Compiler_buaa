package frontend.parser.ast.stmt;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.val.LVal;
import midend.symbol.SymbolManager;

import java.util.ArrayList;

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
        if (getCurrentToken().getType().equals(TokenType.IFTK)) {
            addAndParseNode(new TokenNode());   // 'if'
            addAndParseNode(new TokenNode());   // '('
            addAndParseNode(new Cond());    // Cond
            checkRightParen();   // ')'
            addAndParseNode(new Stmt());    // Stmt
            if (getCurrentToken().getType().equals(TokenType.ELSETK)) {
                addAndParseNode(new TokenNode());   // 'else'
                addAndParseNode(new Stmt());    // Stmt
            }
        }
        // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        else if (getCurrentToken().getType().equals(TokenType.FORTK)) {
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
            // [ForStmt]+
            if (!isRightParenToken() && getCurrentToken().getType().equals(TokenType.IDENFR)) {
                addAndParseNode(new ForStmt());
            }
            checkRightParen();   // ')'
            addAndParseNode(new Stmt());    // Stmt
        }
        // 'break' ';'
        else if (getCurrentToken().getType().equals(TokenType.BREAKTK)) {
            addAndParseNode(new TokenNode());   // 'break'
            checkSemicolon();   // ;
        }
        // 'continue' ';'
        else if (getCurrentToken().getType().equals(TokenType.CONTINUETK)) {
            addAndParseNode(new TokenNode());   // 'continue'
            checkSemicolon();   // ';'
        }
        // 'return' [Exp] ';'
        else if (getCurrentToken().getType().equals(TokenType.RETURNTK)) {
            addAndParseNode(new TokenNode());   // 'return'
            // [Exp]
            if (!isSemicolonToken() && isExp()) {
                addAndParseNode(new Exp());
            }
            checkSemicolon();   // ';'
        }
        // 'printf''('StringConst {','Exp}')'';'
        else if (getCurrentToken().getType().equals(TokenType.PRINTFTK)) {
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
        // LVal '=' Exp ';' 或者 Exp ';'
        else if (getCurrentToken().getType().equals(TokenType.IDENFR)) {
            int originPos = getCurTokenPos();
            LVal lval = new LVal();
            lval.parse();
            // LVal '=' Exp ';'
            if (getCurrentToken().getType().equals(TokenType.ASSIGN)) {
                setTokenStreamPos(originPos);   // 回溯
                addAndParseNode(new LVal());    // LVal
                addAndParseNode(new TokenNode());   // '='
                addAndParseNode(new Exp()); // Exp
                checkSemicolon();   // ';'
            }
            // Exp ';'
            else {
                setTokenStreamPos(originPos);   // 回溯
                addAndParseNode(new Exp()); // Exp
                checkSemicolon();   // ';'
            }
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
        TokenType curTokenType = getCurrentToken().getType();
        return curTokenType.equals(TokenType.LPARENT) || curTokenType.equals(TokenType.INTCON) || curTokenType.equals(TokenType.IDENFR)
                || curTokenType.equals(TokenType.PLUS) || curTokenType.equals(TokenType.MINU) || curTokenType.equals(TokenType.NOT);
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        // 如果是Block，就要进入新的作用域，需要创建字符号表
        if (components.get(0) instanceof Block) {
            SymbolManager.createSonSymbolTable();
            super.visit();
        } else {
            super.visit();
        }
    }
}
