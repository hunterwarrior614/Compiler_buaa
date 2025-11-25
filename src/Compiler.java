import frontend.FrontEnd;
import midend.MidEnd;
import utils.IOHandler;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOHandler.initialize(); // 初始化输入输出流

        FrontEnd.initialize();          // 初始化输入、lexer与parser
        FrontEnd.generateTokenList();   // 词法分析
        FrontEnd.generateAstTree();     // 语法分析

        MidEnd.generateSymbolTable();   // 语义分析
        MidEnd.generateLlvmIr();        // LLVM IR 中间代码生成

        int stage = 4;  // 词法(1)，语法(2)，语义(3)
        IOHandler.print(stage);
    }
}