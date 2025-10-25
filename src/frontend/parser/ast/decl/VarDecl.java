package frontend.parser.ast.decl;

import frontend.lexer.TokenType;
import frontend.parser.ast.def.BType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.def.VarDef;

import java.util.ArrayList;

public class VarDecl extends Node {
    // VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
    public VarDecl() {
        super(SyntaxType.VAR_DECL);
    }

    @Override
    public void parse() {
        // [ 'static' ]
        if (isStaticToken()) {
            addAndParseNode(new TokenNode());
        }
        addAndParseNode(new BType());   // BType
        addAndParseNode(new VarDef());  // VarDef
        while (isCommaToken()) {
            addAndParseNode(new TokenNode());   // ','
            addAndParseNode(new VarDef());  // VarDef
        }
        checkSemicolon();   // ';'
    }

    @Override
    public void visit() {
        ArrayList<Node> components = getComponents();
        int idx = 0;
        boolean isStatic = components.get(idx).isTypeOfToken(TokenType.STATICTK);
        idx += isStatic ? 2 : 1;
        ((VarDef) components.get(idx)).visit(isStatic); // VarDef
        idx++;
        while (components.get(idx).isTypeOfToken(TokenType.COMMA)) {
            ((VarDef) components.get(idx + 1)).visit(isStatic);
            idx += 2;
        }
    }
}
