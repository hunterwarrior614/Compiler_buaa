package utils;

import frontend.FrontEnd;
import error.Error;
import error.ErrorRecorder;
import frontend.lexer.Token;
import frontend.parser.ast.Node;
import midend.MidEnd;
import midend.llvm.IrModule;
import midend.symbol.SymbolTable;

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
    private static FileOutputStream symbolOutput = null;
    private static FileOutputStream llvmIrOutput = null;
    private static FileOutputStream errorOutput = null;

    public static void initialize() throws FileNotFoundException {
        input = new PushbackInputStream(new FileInputStream("testfile.txt"), 16);
        lexerOutput = new FileOutputStream("lexer.txt");
        parserOutput = new FileOutputStream("parser.txt");
        symbolOutput = new FileOutputStream("symbol.txt");
        errorOutput = new FileOutputStream("error.txt");
        llvmIrOutput = new FileOutputStream("llvm_ir.txt");
    }

    public static PushbackInputStream getInput() {
        return input;
    }

    public static void print(int stage) throws IOException {
        if (ErrorRecorder.hasErrors()) {
            IOHandler.printError(stage);
        } else {
            switch (stage) {
                case 1:
                    IOHandler.printTokenList(); // 输出词法分析
                    break;
                case 2:
                    IOHandler.printAstTree();   // 输出语法分析
                    break;
                case 3:
                    IOHandler.printSymbolTable();   // 输出语义分析
                    break;
                case 4:
                    IOHandler.printLlvmIr();
            }
        }
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

    public static void printSymbolTable() throws IOException {
        SymbolTable symbolTable = MidEnd.getSymbolTable();
        symbolOutput.write(symbolTable.toString().getBytes());
        System.out.println("symbol.txt 输出完毕");
    }

    public static void printLlvmIr() throws IOException {
        IrModule irModule = MidEnd.getIrModule();
        llvmIrOutput.write(irModule.toString().getBytes());
        System.out.println("llvm_ir.txt 输出完毕");
    }

    public static void printError(int stage) throws IOException {
        ArrayList<Error> errors = filterErrors(ErrorRecorder.getErrors(), stage);
        for (Error error : errors) {
            errorOutput.write((error + "\n").getBytes());
        }
        if (!errors.isEmpty()) {
            System.out.println("error.txt 输出完毕");
        }
    }

    private static ArrayList<Error> filterErrors(ArrayList<Error> errors, int stage) {
        ArrayList<Error> filteredErrors = new ArrayList<>();
        for (Error error : errors) {
            switch (stage) {
                // 仅输出a类错误
                case 1:
                    if (error.getType() == Error.Type.a) {
                        filteredErrors.add(error);
                    }
                    break;
                // 仅输出a, i, j, k类错误
                case 2:
                    if (error.getType() == Error.Type.a || error.getType() == Error.Type.i ||
                            error.getType() == Error.Type.j || error.getType() == Error.Type.k) {
                        filteredErrors.add(error);
                    }
                    break;
                // 全部输出
                case 3:
                    filteredErrors.add(error);
            }
        }
        return filteredErrors;
    }
}
