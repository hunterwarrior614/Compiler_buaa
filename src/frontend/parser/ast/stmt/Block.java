package frontend.parser.ast.stmt;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.SymbolManager;

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
        super.visit();
        SymbolManager.goBackToParentSymbolTable();  // 退出Block时，相当于退出当前作用域，需要返回到父符号表
    }
}
