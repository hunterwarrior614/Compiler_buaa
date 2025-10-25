package frontend.parser;

import frontend.lexer.Token;
import frontend.lexer.TokenStream;
import frontend.parser.ast.CompUnit;
import frontend.parser.ast.Node;

import java.util.ArrayList;

public class Parser {
    private final CompUnit rootNode;

    public Parser(ArrayList<Token> tokens) {
        Node.initialize(new TokenStream(tokens));
        rootNode = new CompUnit();
    }

    public void parse() {
        rootNode.parse();
    }

    public CompUnit getAstTree() {
        return rootNode;
    }
}
