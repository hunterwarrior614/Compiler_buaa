package utils;

import frontend.FrontEnd;
import frontend.error.Error;
import frontend.error.ErrorRecorder;
import frontend.lexer.Token;
import frontend.parser.ast.Node;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class IOHandler {
    private static PushbackInputStream input = null;
    private static FileOutputStream lexerOutput = null;
    private static FileOutputStream parserOutput = null;
    private static FileOutputStream errorOutput = null;

    public static void initialize() throws FileNotFoundException {
        input = new PushbackInputStream(new FileInputStream("testfile.txt"), 16);
        lexerOutput = new FileOutputStream("lexer.txt");
        parserOutput = new FileOutputStream("parser.txt");
        errorOutput = new FileOutputStream("error.txt");
    }

    public static PushbackInputStream getInput() {
        return input;
    }

    public static void printTokenList() throws IOException {
        for (Token token : FrontEnd.getTokenList()) {
            lexerOutput.write((token + "\n").getBytes());
        }
        System.out.println("lexer.txt 输出完毕");
    }

    public static void printAstTree() throws IOException {
        Node astTree = FrontEnd.getAstTree();
        parserOutput.write(astTree.toString().getBytes());
        System.out.println("parser.txt 输出完毕");
    }

    public static void printError() throws IOException {
        ArrayList<Error> errors = ErrorRecorder.getErrors();
        for (Error error : errors) {
            errorOutput.write((error + "\n").getBytes());
        }
        if (!errors.isEmpty()) {
            System.out.println("error.txt 输出完毕");
        }
    }
}
