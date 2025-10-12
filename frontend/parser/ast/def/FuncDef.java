package frontend.parser.ast.def;

import frontend.lexer.Token;
import frontend.parser.ast.stmt.Block;
import frontend.parser.ast.param.FuncFParams;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class FuncDef extends Node {
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public FuncDef() {
        super(SyntaxType.FUNC_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new FuncType());    // FuncType
        addAndParseNode(new TokenNode());   // Ident
        addAndParseNode(new TokenNode());   // '('
        if (!isRightParenToken() && getCurrentToken().getType().equals(Token.Type.INTTK)) {
            addAndParseNode(new FuncFParams()); // FuncFParams
        }
        checkRightParen();   // ')'
        addAndParseNode(new Block());   // Block
    }
}
