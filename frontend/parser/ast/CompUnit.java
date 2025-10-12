package frontend.parser.ast;

import frontend.lexer.Token;
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
        Token.Type curTokenType = getCurrentToken().getType();
        Token.Type preTokenType = peekToken(1).getType();
        Token.Type prePreTokenType = peekToken(2).getType();
        return curTokenType.equals(Token.Type.VOIDTK) ||
                curTokenType.equals(Token.Type.INTTK) && preTokenType.equals(Token.Type.IDENFR) && prePreTokenType.equals(Token.Type.LPARENT);
    }

    private boolean isMainFuncDef() {
        Token.Type curTokenType = getCurrentToken().getType();
        Token.Type preTokenType = peekToken(1).getType();
        return curTokenType.equals(Token.Type.INTTK) && preTokenType.equals(Token.Type.MAINTK);
    }
}
