package frontend.parser.ast.def;

import frontend.lexer.Token;
import frontend.lexer.TokenType;
import frontend.parser.ast.stmt.Block;
import frontend.parser.ast.param.FuncFParams;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.Symbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

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
        if (!isRightParenToken() && getCurrentToken().getType().equals(TokenType.INTTK)) {
            addAndParseNode(new FuncFParams()); // FuncFParams
        }
        checkRightParen();   // ')'
        addAndParseNode(new Block());   // Block
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        TokenType funcType = ((FuncType) components.get(0)).getFuncType();
        String symbolName = ((TokenNode) components.get(1)).getTokenValue();
        SymbolType symbolType = funcType.equals(TokenType.INTTK) ? SymbolType.INT_FUNC : SymbolType.VOID_FUNC;

        SymbolManager.addSymbol(symbolType, symbolName);    // 将函数填入当前符号表
        SymbolManager.createSonSymbolTable();   // 即将进入内层作用域，创建子符号表
        // 无参数
        if (components.get(3).isTypeOfToken(TokenType.RPARENT)) {
            components.get(4).visit();  // Block
        }
        // 有参数
        else {
            components.get(3).visit();  // FuncFParams
            components.get(5).visit();  // Block
        }
    }
}
