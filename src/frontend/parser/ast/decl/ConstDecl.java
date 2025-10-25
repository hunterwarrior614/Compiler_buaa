package frontend.parser.ast.decl;

import frontend.lexer.TokenType;
import frontend.parser.ast.def.BType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.def.ConstDef;

import java.util.ArrayList;

public class ConstDecl extends Node {
    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'

    public ConstDecl() {
        super(SyntaxType.CONST_DECL);
    }

    @Override
    public void parse() {
        addAndParseNode(new TokenNode());   // 'const'
        addAndParseNode(new BType());   // BType
        addAndParseNode(new ConstDef());    // ConstDef
        // {',' ConstDef}
        while (isCommaToken()) {
            addAndParseNode(new TokenNode());
            addAndParseNode(new ConstDef());
        }
        // ';'
        checkSemicolon();
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        components.get(2).visit();  // ConstDef
        int idx = 3;
        while (components.get(idx).isTypeOfToken(TokenType.COMMA)) {
            components.get(idx + 1).visit();
            idx += 2;
        }
    }
}
