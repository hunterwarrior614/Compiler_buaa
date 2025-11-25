package frontend.parser.ast.stmt;

import error.Error;
import error.ErrorRecorder;
import frontend.lexer.TokenType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class Block extends Node {
    // Block → '{' { BlockItem } '}'

    public Block() {
        super(SyntaxType.BLOCK);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());   // '{'
        while (!isRightBraceToken()) {
            addAndParseNode(new BlockItem());   // BlockItem
        }
        addAndParseNode(new TokenNode());   // '}'
    }

    @Override
    public void visit() {
        for (Node node : components) {
            node.visit();
            checkReturn(node);
        }
        SymbolManager.goBackToParentSymbolTable();  // 退出Block时，相当于退出当前作用域，需要返回到父符号表
        SymbolManager.goOutOfBlock();
    }

    public void checkReturn(Node node) {
        if (!(node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.RBRACE))) {
            return;
        }

        // 有返回值的函数缺少return语句（只需要考虑函数末尾是否存在return语句）
        if (SymbolManager.getCurrentFuncType() == SymbolType.INT_FUNC
                && !SymbolManager.inInnerBlock()    // 在函数体最外层
                && !SymbolManager.hasReturn()) {
            ErrorRecorder.addError(new Error(Error.Type.g, tokenNode.getLineNumber()));
        }
    }

    // LLVM IR
    public ArrayList<BlockItem> getBlockItems() {
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        for (Node node : components) {
            if (node instanceof BlockItem blockItem) {
                blockItems.add(blockItem);
            }
        }
        return blockItems;
    }
}
