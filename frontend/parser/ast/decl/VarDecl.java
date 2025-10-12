package frontend.parser.ast.decl;

import frontend.parser.ast.def.BType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.def.VarDef;

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
}
