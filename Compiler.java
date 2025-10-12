import frontend.FrontEnd;
import frontend.error.ErrorRecorder;
import utils.IOHandler;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOHandler.initialize(); // 初始化输入输出流

        FrontEnd.initialize();          // 初始化输入、lexer与parser
        FrontEnd.generateTokenList();   // 词法分析
        FrontEnd.generateAstTree();     // 语法分析

        if (ErrorRecorder.hasErrors()) {
            IOHandler.printError();
        } else {
            // IOHandler.printTokenList(); // 输出词法分析
            IOHandler.printAstTree();   // 输出语法分析
        }
    }
}