package frontend.parser.ast.param;

import frontend.parser.ast.def.BType;
import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;
import frontend.parser.ast.TokenNode;

public class FuncFParam extends Node {
    // FuncFParam → BType Ident ['[' ']']
    public FuncFParam() {
        super(SyntaxType.FUNC_FORMAL_PARAM);
    }

    @Override
    public void parse() {
        addAndParseNode(new BType());   // BType
        addAndParseNode(new TokenNode());   // Ident
        // ['[' ']']
        if (isLeftBracketToken()) {
            addAndParseNode(new TokenNode());   // '['
            checkRightBracket();   // ']'
        }
    }
}
