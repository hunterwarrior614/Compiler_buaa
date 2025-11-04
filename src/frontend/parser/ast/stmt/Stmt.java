package frontend.parser.ast.stmt;

import error.Error;
import error.ErrorRecorder;
import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.val.LVal;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

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

    private int printfFStrCount = 0;
    private int printfExpCount = 0;


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
            TokenNode strConToken = new TokenNode();
            addAndParseNode(strConToken);   // StringConst
            countPrintfFStr(strConToken);
            while (isCommaToken()) {
                addAndParseNode(new TokenNode());   // ','
                addAndParseNode(new Exp()); // Exp
                printfExpCount++;
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
            int errorCount = ErrorRecorder.getErrorsCount();
            LVal lval = new LVal();
            lval.parse();
            // LVal '=' Exp ';'
            if (getCurrentToken().getType().equals(TokenType.ASSIGN)) {
                reset(originPos, errorCount);   // 回溯
                addAndParseNode(new LVal());    // LVal
                addAndParseNode(new TokenNode());   // '='
                addAndParseNode(new Exp()); // Exp
                checkSemicolon();   // ';'
            }
            // Exp ';'
            else {
                reset(originPos, errorCount);   // 回溯
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
        }

        boolean forStmt = false;
        boolean returnStmt = false;
        boolean hasExpValue = false;
        int returnLineNumber = 0;
        for (Node node : components) {
            if (node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.FORTK)) {
                forStmt = true;
            }
            if (forStmt && node instanceof Stmt) {
                SymbolManager.goIntoLoopBlock();   // 进入for语句块
            }
            if (node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.RETURNTK)) {
                returnStmt = true;
                returnLineNumber = tokenNode.getLineNumber();
            }
            if (node instanceof Exp) {
                hasExpValue = true;
            }
            if (node instanceof Block) {
                SymbolManager.goIntoBlock();
            }

            node.visit();
            checkReturn(node, returnStmt, hasExpValue, returnLineNumber);
            checkModified(node);
            checkPrintf(node);
            checkBreakOrContinue(node);
        }

        if (forStmt) {
            SymbolManager.goOutOfLoopBlock();   // 最后要出for语句块
        }
    }


    private void checkReturn(Node node, boolean returnStmt, boolean hasReturnValue, int returnLineNumber) {
        if (!returnStmt || !(node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.SEMICN))) {
            return;
        }

        SymbolManager.setHasReturn();  // 遇见return
        // 无返回值的函数存在不匹配的return语句
        if (SymbolManager.getCurrentFuncType() == SymbolType.VOID_FUNC && hasReturnValue) {
            ErrorRecorder.addError(new Error(Error.Type.f, returnLineNumber));
        }
    }

    private void checkModified(Node node) {
        if (!(node instanceof LVal lVal)) {
            return;
        }
        int lValLineNumber = lVal.getLineNumber();
        // 不能改变常量的值
        if (lVal.isConstVar()) {
            ErrorRecorder.addError(new Error(Error.Type.h, lValLineNumber));
        }
    }

    private void countPrintfFStr(TokenNode strConToken) {
        String conStr = strConToken.getTokenValue();
        for (int i = 0; i < conStr.length() - 1; i++) {
            if (conStr.charAt(i) == '%' && conStr.charAt(i + 1) == 'd') {
                printfFStrCount++;
            }
        }
    }

    private void checkPrintf(Node node) {
        if (!(node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.PRINTFTK))) {
            return;
        }
        int printfLineNumber = tokenNode.getLineNumber();
        //  printf中格式字符与表达式个数不匹配
        if (printfFStrCount != printfExpCount) {
            ErrorRecorder.addError(new Error(Error.Type.l, printfLineNumber));
        }
    }

    private void checkBreakOrContinue(Node node) {
        if (!(node instanceof TokenNode tokenNode && (tokenNode.isTypeOfToken(TokenType.BREAKTK) || tokenNode.isTypeOfToken(TokenType.CONTINUETK)))) {
            return;
        }
        int errorLineNumber = tokenNode.getLineNumber();
        // 在非循环块中使用break和continue语句
        if (!SymbolManager.inLoopBlock()) {
            ErrorRecorder.addError(new Error(Error.Type.m, errorLineNumber));
        }
    }
}
