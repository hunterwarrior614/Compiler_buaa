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
    private SymbolType funcType = null;
    private boolean hasReturnValue = false;

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
        ArrayList<Node> components = getComponents();
        for (Node node : components) {
            if (node instanceof BlockItem blockItem) {
                blockItem.setFuncType(funcType);
                hasReturnValue = blockItem.hasReturnValue();
            }
            node.visit();
            checkReturn(node);
        }
        SymbolManager.goBackToParentSymbolTable();  // 退出Block时，相当于退出当前作用域，需要返回到父符号表
    }

    public void setFuncType(SymbolType funcType) {
        this.funcType = funcType;
    }

    public void checkReturn(Node node) {
        if (funcType == null || !(node instanceof TokenNode tokenNode && tokenNode.isTypeOfToken(TokenType.RBRACE))) {
            return;
        }
        int rightBraceLineNumber = tokenNode.getLineNumber();
        // 有返回值的函数缺少return语句
        if (funcType == SymbolType.INT_FUNC && !hasReturnValue) {
            ErrorRecorder.addError(new Error(Error.Type.g, rightBraceLineNumber));
        }
    }
}
