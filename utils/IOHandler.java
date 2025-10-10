package utils;

import frontend.FrontEnd;
import frontend.error.Error;
import frontend.error.ErrorRecorder;
import frontend.lexer.Token;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class IOHandler {
    private static PushbackInputStream input = null;
    private static FileOutputStream lexerOutput = null;
    private static FileOutputStream errorOutput = null;

    public static void initialize() throws FileNotFoundException {
        input = new PushbackInputStream(new FileInputStream("testfile.txt"), 16);
        lexerOutput = new FileOutputStream("lexer.txt");
        errorOutput = new FileOutputStream("error.txt");
    }

    public static PushbackInputStream getInput() {
        return input;
    }

    public static void printTokenList() throws IOException {
        ArrayList<Token> tokens = FrontEnd.getTokenList();
        for (Token token : tokens) {
            lexerOutput.write((token + "\n").getBytes());
        }
        if (!tokens.isEmpty()) {
            System.out.println("lexer.txt 输出完毕");
        }
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
