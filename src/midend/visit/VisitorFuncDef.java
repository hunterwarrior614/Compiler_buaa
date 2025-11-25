package midend.visit;

import frontend.parser.ast.def.FuncDef;
import frontend.parser.ast.def.MainFuncDef;
import frontend.parser.ast.stmt.Block;
import midend.llvm.instr.AllocateInstr;
import midend.llvm.instr.StoreInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.IrBuilder;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrParameter;
import midend.symbol.FuncSymbol;
import midend.symbol.SymbolManager;
import midend.symbol.SymbolType;
import midend.symbol.ValueSymbol;

import java.util.ArrayList;

public class VisitorFuncDef {
    public static void visitComFuncDef(FuncDef funcDef) {
        FuncSymbol funcSymbol = funcDef.getFuncSymbol();
        IrFunc irFunc = IrBuilder.createIrFunc(getFuncIrBaseType(funcSymbol.getType()), funcSymbol.getName());
        funcSymbol.setIrValue(irFunc);

        SymbolManager.goToSonSymbolTable();
        // 创建参数 IR
        ArrayList<ValueSymbol> paramSymbols = funcSymbol.getParamSymbols();
        ArrayList<IrParameter> irParameters = new ArrayList<>();
        for (ValueSymbol paramSymbol : paramSymbols) {
            IrParameter irParameter = new IrParameter(paramSymbol.getIrBaseType(), IrBuilder.getLocalVarName());
            irFunc.addParameter(irParameter);
            irParameters.add(irParameter);
        }
        // 值传递
        for (int i = 0; i < irParameters.size(); i++) {
            // 有 n 个参数就分配 n 个变量，并将参数值传递给相应的变量
            IrParameter irParameter = irParameters.get(i);
            ValueSymbol paramSymbol = paramSymbols.get(i);

            AllocateInstr allocateInstr = new AllocateInstr(paramSymbol.getIrBaseType());  // 分配变量
            paramSymbol.setIrValue(allocateInstr);

            new StoreInstr(irParameter, allocateInstr); // 将参数值传递给变量
        }

        // 创建 Block IR
        Block block = funcDef.getBlock();
        VisitorBlock.visitBlock(block);

        SymbolManager.goBackToParentSymbolTable();
    }

    public static void visitMainFuncDef(MainFuncDef mainFuncDef) {
        IrBuilder.createIrFunc(new IrBaseType(IrBaseType.TypeValue.INT32), "main");

        SymbolManager.goToSonSymbolTable();
        // 创建 Block IR
        Block block = mainFuncDef.getBlock();
        VisitorBlock.visitBlock(block);

        SymbolManager.goBackToParentSymbolTable();
    }

    private static IrBaseType getFuncIrBaseType(SymbolType funcType) {
        return switch (funcType) {
            case INT_FUNC -> new IrBaseType(IrBaseType.TypeValue.INT32);
            case VOID_FUNC -> new IrBaseType(IrBaseType.TypeValue.VOID);
            default -> throw new Error("[ERROR] Unknown function type");
        };
    }
}
