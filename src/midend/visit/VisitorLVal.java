package midend.visit;

import frontend.parser.ast.val.LVal;
import midend.llvm.constant.IrConstInt;
import midend.llvm.instr.GetElemInstr;
import midend.llvm.instr.LoadInstr;
import midend.llvm.type.IrBaseType;
import midend.llvm.value.IrValue;
import midend.symbol.SymbolManager;
import midend.symbol.ValueSymbol;

public class VisitorLVal {
    public static IrValue visitLVal(LVal lval, boolean leftValue) {
        // LVal 被访问时有两种情况：
        // 作为左值：Stmt/ForStmt → LVal '=' Exp
        // 作为右值：PrimaryExp → LVal
        return leftValue ? visitLValAsLValue(lval) : visitLValAsRValue(lval);
    }

    private static IrValue visitLValAsLValue(LVal lval) {
        ValueSymbol valueSymbol = (ValueSymbol) SymbolManager.getSymbol(lval.getIdentName());
        if (valueSymbol == null) {
            throw new RuntimeException("[ERROR] Symbol not found in LLVM IR");
        }

        // 非数组，直接返回地址
        if (valueSymbol.getDimension() == 0) {
            return valueSymbol.getIrValue();
        }
        // 数组
        else {
            IrValue arrayPointer = valueSymbol.getIrValue();    // 获取数组基地址
            IrValue index = VisitorExp.visitExp(lval.getIndexExp());    // 获取索引
            /*  arrayPointer 是数组指针，直接从数组加载
                %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3
             */
            if (arrayPointer.getIrBaseType().getPointValueTypeValue().equals(IrBaseType.TypeValue.INT_ARRAY)) {
                return new GetElemInstr(arrayPointer, index);   // 获取到目标元素地址
            }
            /*  arrayPointer 是二级指针，先获取到首元素地址，再加载
                %2 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 0
                %3 = getelementptr i32, i32* %2, i32 3
             */
            else {
                IrValue basePointer = new LoadInstr(arrayPointer);  // 获取到首元素地址
                return new GetElemInstr(basePointer, index); // 获取到目标元素地址
            }
        }
    }

    private static IrValue visitLValAsRValue(LVal lval) {
        ValueSymbol valueSymbol = (ValueSymbol) SymbolManager.getSymbol(lval.getIdentName());
        if (valueSymbol == null) {
            throw new RuntimeException("[ERROR] Symbol not found in LLVM IR");
        }

        // 非数组，需加载值到内存
        if (valueSymbol.getDimension() == 0) {
            return new LoadInstr(valueSymbol.getIrValue());
        }
        // 数组
        else {
            IrValue arrayPointer = valueSymbol.getIrValue();    // 获取数组基地址
            // 如果没有索引，则直接获取数组首地址
            if (lval.getIndexExp() == null) {
                return new GetElemInstr(arrayPointer, new IrConstInt(0));
            } else {
                IrValue index = VisitorExp.visitExp(lval.getIndexExp());    // 获取索引
                // arrayPointer 是数组指针，直接从数组加载
                if (arrayPointer.getIrBaseType().getPointValueTypeValue().equals(IrBaseType.TypeValue.INT_ARRAY)) {
                    IrValue elemPointer = new GetElemInstr(arrayPointer, index);    // 获取到目标元素地址
                    return new LoadInstr(elemPointer);
                }
                // arrayPointer 是二级指针，先获取到首元素地址，再加载
                else {
                    IrValue basePointer = new LoadInstr(arrayPointer);  // 获取到首元素地址
                    IrValue element = new GetElemInstr(basePointer, index); // 获取到目标元素地址
                    return new LoadInstr(element);
                }
            }
        }
    }
}
