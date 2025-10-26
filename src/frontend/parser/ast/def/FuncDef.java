package frontend.parser.ast.def;

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
            setFuncType(node, symbolType);
            node.visit();
            if (node instanceof FuncFParams funcFParams) {
                func.setParamsType(funcFParams.getParamsType());
            }
        }
    }

    public void setFuncType(Node node, SymbolType funcType) {
        if (!(node instanceof Block block)) {
            return;
        }
        block.setFuncType(funcType);
    }
}
