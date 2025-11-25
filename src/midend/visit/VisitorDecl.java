package midend.visit;

import frontend.parser.ast.decl.ConstDecl;
import frontend.parser.ast.decl.Decl;
import frontend.parser.ast.decl.VarDecl;
import frontend.parser.ast.def.ConstDef;
import frontend.parser.ast.def.VarDef;
import frontend.parser.ast.exp.Exp;
import midend.llvm.IrBuilder;
import midend.llvm.constant.IrConst;
import midend.llvm.constant.IrConstInt;
import midend.llvm.constant.IrConstIntArray;
import midend.llvm.instr.AllocateInstr;
import midend.llvm.instr.GetElemInstr;
import midend.llvm.instr.StoreInstr;
import midend.llvm.value.IrGlobalVariable;
import midend.llvm.value.IrValue;
import midend.symbol.SymbolManager;
import midend.symbol.ValueSymbol;

import java.util.ArrayList;

public class VisitorDecl {
    public static void visitDecl(Decl decl) {
        if (decl.isConstDecl()) {
            visitConstDecl((ConstDecl) decl.getDecl());
        } else {
            visitVarDecl((VarDecl) decl.getDecl());
        }
    }

    public static void visitConstDecl(ConstDecl constDecl) {
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitConstDef(constDef);
        }
    }

    private static void visitVarDecl(VarDecl varDecl) {
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitVarDef(varDef);
        }
    }

    private static void visitConstDef(ConstDef constDef) {
        ValueSymbol valueSymbol = (ValueSymbol) SymbolManager.getSymbol(constDef.getIdentName());
        if (valueSymbol == null) {
            throw new RuntimeException("[ERROR] Symbol not found in LLVM IR");
        }

        constDef.setValueToSymbol();    // 将所赋的初值给对应的valueSymbol
        // 全局作用域
        if (SymbolManager.isGlobal()) {
            // TODO:是否需要对POINTER做限制(CONST)
            IrGlobalVariable irGlobalVariable = IrBuilder.createIrGlobalVariable("@" + valueSymbol.getName(), getIrConst(valueSymbol));
            valueSymbol.setIrValue(irGlobalVariable);
        }
        // 局部作用域
        else {
            visitLocalConstDef(constDef, valueSymbol);
        }
    }

    private static void visitVarDef(VarDef varDef) {
        ValueSymbol valueSymbol = (ValueSymbol) SymbolManager.getSymbol(varDef.getIdentName());
        if (valueSymbol == null) {
            throw new RuntimeException("[ERROR] Symbol not found in LLVM IR");
        }

        // 全局作用域
        if (SymbolManager.isGlobal()) {
            varDef.setValueToSymbol();  // 将变量的初值赋给valueSymbol
            IrGlobalVariable irGlobalVariable = IrBuilder.createIrGlobalVariable("@" + valueSymbol.getName(), getIrConst(valueSymbol));
            valueSymbol.setIrValue(irGlobalVariable);
        }
        // 局部作用域
        else {
            visitLocalVarDef(varDef, valueSymbol);
        }
    }

    private static void visitLocalConstDef(ConstDef constDef, ValueSymbol valueSymbol) {
        AllocateInstr allocateInstr = new AllocateInstr(valueSymbol.getIrBaseType());
        valueSymbol.setIrValue(allocateInstr);

        ArrayList<Integer> valueList = valueSymbol.getValueList();
        // 若非数组，则添加存储指令
        if (valueSymbol.getDimension() == 0) {
            if (valueList.isEmpty()) {
                valueList.add(0);
            }
            IrValue initValue = new IrConstInt(valueList.get(0));

            new StoreInstr(initValue, allocateInstr);
        }
        // 数组
        else {
            ArrayList<Integer> initList = constDef.getConstInitVal().getValueList();
            for (int i = 0; i < initList.size(); i++) {
                IrValue initValue = new IrConstInt(initList.get(i));
                GetElemInstr getElemInstr = new GetElemInstr(allocateInstr, new IrConstInt(i)); // 获取第i个数组元素

                new StoreInstr(initValue, getElemInstr);    // 将初值赋给元素
            }
        }
    }

    private static void visitLocalVarDef(VarDef varDef, ValueSymbol valueSymbol) {
        AllocateInstr allocateInstr = new AllocateInstr(valueSymbol.getIrBaseType());
        valueSymbol.setIrValue(allocateInstr);
        // 若非数组，则添加存储指令
        if (valueSymbol.getDimension() == 0) {
            // 若有初值，进行赋值
            if (varDef.hasInitVal()) {
                Exp exp = varDef.getInitVal().getExpList().get(0);
                IrValue initValue = VisitorExp.visitExp(exp);

                new StoreInstr(initValue, allocateInstr);
            }
        }
        // 数组
        else {
            ArrayList<Exp> initList = varDef.getInitVal().getExpList();
            for (int i = 0; i < initList.size(); i++) {
                IrValue initValue = VisitorExp.visitExp(initList.get(i));
                GetElemInstr getElemInstr = new GetElemInstr(allocateInstr, new IrConstInt(i)); // 获取第i个数组元素

                new StoreInstr(initValue, getElemInstr);    // 将初值赋给元素
            }
        }
    }

    // 全局变量/数组
    private static IrConst getIrConst(ValueSymbol valueSymbol) {
        ArrayList<Integer> valueList = valueSymbol.getValueList();
        // 非数组类型
        if (valueSymbol.getDimension() == 0) {
            if (valueList.isEmpty()) {
                valueList.add(0);   // 这里严格来说是全局设为0，局部值来源于内存
            }
            return new IrConstInt(valueList.get(0));
        }
        // 数组
        else {
            int arrayLength = valueSymbol.getLength();
            ArrayList<IrConstInt> initialList = new ArrayList<>();

            if (!valueList.isEmpty()) {
                for (Integer num : valueList) {
                    initialList.add(new IrConstInt(num));
                }
                for (int i = valueList.size(); i < arrayLength; i++) {
                    initialList.add(new IrConstInt(0));
                }
            }

            return new IrConstIntArray(initialList, arrayLength);
        }
    }
}
