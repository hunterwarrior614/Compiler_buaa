import backend.BackEnd;
import error.ErrorRecorder;
import frontend.FrontEnd;
import midend.MidEnd;
import optimize.OptimizeManager;
import utils.IOHandler;
import utils.Settings;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        IOHandler.initialize(); // 初始化输入输出流

        FrontEnd.initialize();          // 初始化输入、lexer与parser
        FrontEnd.generateTokenList();   // 词法分析
        FrontEnd.generateAstTree();     // 语法分析

        MidEnd.generateSymbolTable();   // 语义分析

        if (!ErrorRecorder.hasErrors()) {
            MidEnd.generateLlvmIr();        // LLVM IR 中间代码生成
            BackEnd.generateMips();         // Mips 目标代码生成

            if (Settings.FINE_TUNING) {
                OptimizeManager.Init();
                OptimizeManager.Optimize();
            }
        }

        int stage = 5;  // 词法(1)，语法(2)，语义(3)
        IOHandler.print(stage);
    }
}