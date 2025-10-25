package frontend.parser.ast;

import frontend.lexer.TokenType;
import frontend.parser.ast.decl.Decl;
import frontend.parser.ast.def.FuncDef;
import frontend.parser.ast.def.MainFuncDef;

public class CompUnit extends Node {
    // CompUnit → {Decl} {FuncDef} MainFuncDef

    public CompUnit() {
        super(SyntaxType.COMP_UNIT);
    }

    @Override
    public void parse() {
        // {Decl}
        while (!isFuncDef() && !isMainFuncDef()) {
            addAndParseNode(new Decl());
        }
        // {FuncDef}
        while (isFuncDef()) {
            addAndParseNode(new FuncDef());
        }
        // MainFuncDef
        addAndParseNode(new MainFuncDef());
    }

    private boolean isFuncDef() {
        TokenType curTokenType = getCurrentToken().getType();
        TokenType preTokenType = peekToken(1).getType();
        TokenType prePreTokenType = peekToken(2).getType();
        return curTokenType.equals(TokenType.VOIDTK) ||
                curTokenType.equals(TokenType.INTTK) && preTokenType.equals(TokenType.IDENFR) && prePreTokenType.equals(TokenType.LPARENT);
    }

    private boolean isMainFuncDef() {
        TokenType curTokenType = getCurrentToken().getType();
        TokenType preTokenType = peekToken(1).getType();
        return curTokenType.equals(TokenType.INTTK) && preTokenType.equals(TokenType.MAINTK);
    }
}
