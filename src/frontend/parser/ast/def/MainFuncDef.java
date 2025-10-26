package frontend.parser.ast.def;

import frontend.parser.ast.stmt.Block;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import midend.symbol.SymbolManager;

import java.util.ArrayList;

public class MainFuncDef extends Node {
    //  MainFuncDef → 'int' 'main' '(' ')' Block
    public MainFuncDef() {
        super(SyntaxType.MAIN_FUNC_DEF);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());   // 'int'
        addAndParseNode(new TokenNode());   // 'main'
        addAndParseNode(new TokenNode());   // '('
        checkRightParen();   // ')'
        addAndParseNode(new Block());   // Block
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        for (Node node : components) {
            if (node instanceof Block block) {
                SymbolManager.createSonSymbolTable();
                block.visit();
            }
        }
    }
}
