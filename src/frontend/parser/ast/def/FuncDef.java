package frontend.parser.ast.def;

import error.Error;
import error.ErrorRecorder;
import frontend.lexer.TokenType;
import frontend.parser.ast.Ident;
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
        addAndParseNode(new Ident());   // Ident
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

        SymbolType symbolType = ((FuncType) components.get(0)).getFuncType().equals(TokenType.INTTK) ? SymbolType.INT_FUNC : SymbolType.VOID_FUNC;
        Ident ident = (Ident) components.get(1);
        Symbol func = new Symbol(symbolType, ident.getTokenValue(), ident.getLineNumber());
        SymbolManager.addSymbol(func);    // 将函数填入当前符号表
        SymbolManager.createSonSymbolTable();   // 即将进入内层作用域，创建子符号表

        for (Node node : components) {
            if (node instanceof Block) {
                SymbolManager.goIntoFuncBlock(symbolType);  // 进函数体
            }
            node.visit();
            if (node instanceof FuncFParams funcFParams) {
                func.setParamsType(funcFParams.getParamsType());
            }
        }

        SymbolManager.goOutOfFuncBlock();   // 最后要出函数体
    }
}
