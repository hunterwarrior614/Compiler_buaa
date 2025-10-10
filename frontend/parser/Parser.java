package frontend.parser;

import frontend.lexer.Token;
import frontend.parser.ast.CompUnit;
import frontend.parser.ast.FirstSet;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<Token> tokens;
    private CompUnit astTree;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        FirstSet.initialize();
    }

    public void parse() {
        // astTree = new CompUnit();
    }
}
