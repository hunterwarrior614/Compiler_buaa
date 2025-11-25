package midend;

import frontend.FrontEnd;
import frontend.parser.ast.CompUnit;
import midend.llvm.IrBuilder;
import midend.llvm.IrModule;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolTable;
import midend.visit.VisitorCompUnit;

public class MidEnd {
    private static CompUnit rootNode;
    private static IrModule irModule;

    public static void generateSymbolTable() {
        SymbolManager.initialize();
        rootNode = FrontEnd.getAstTree();
        rootNode.visit();
    }

    public static SymbolTable getSymbolTable() {
        SymbolManager.deleteSystemSymbol();
        return SymbolManager.getRootSymbolTable();
    }

    public static void generateLlvmIr() {
        irModule = new IrModule();
        IrBuilder.setIrModule(irModule);
        VisitorCompUnit visitor = new VisitorCompUnit(rootNode);
        visitor.visitCompUnit();
        irModule.checkEmptyBasicBlocks();   // 检查是否有空基本块（LLVM IR不允许有）
    }

    public static IrModule getIrModule() {
        return irModule;
    }
}
