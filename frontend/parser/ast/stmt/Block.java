package frontend.parser.ast.stmt;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

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
}
