import frontend.lexer.Lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String input = Files.readString(Paths.get("testfile.txt"));

        Lexer lexer = new Lexer(input);

        lexer.writeToFile();
    }
}