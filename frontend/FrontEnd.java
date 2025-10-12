package frontend;

import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.parser.ast.Node;
import utils.IOHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FrontEnd {
    private static Lexer lexer;
    private static Parser parser;

    public static void initialize() throws IOException {
        lexer = new Lexer(IOHandler.getInput());
    }

    public static void generateTokenList() throws IOException {
        lexer.analyse();
    }

    public static void generateAstTree() {
        parser = new Parser(lexer.getTokenList());
        parser.parse();
    }

    public static ArrayList<Token> getTokenList() {
        return lexer.getTokenList();
    }

    public static Node getAstTree() {
        return parser.getAstTree();
    }
}
