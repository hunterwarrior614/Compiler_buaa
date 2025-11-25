package midend.visit;

import frontend.parser.ast.Node;
import frontend.parser.ast.TokenNode;
import frontend.parser.ast.exp.AddExp;
import frontend.parser.ast.exp.EqExp;
import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.exp.LAndExp;
import frontend.parser.ast.exp.LOrExp;
import frontend.parser.ast.exp.MulExp;
import frontend.parser.ast.exp.PrimaryExp;
import frontend.parser.ast.exp.RelExp;
import frontend.parser.ast.exp.UnaryExp;
import frontend.parser.ast.stmt.Cond;
import frontend.parser.ast.val.LVal;
import midend.llvm.IrBuilder;
import midend.llvm.constant.IrConstInt;
import midend.llvm.instr.AluInstr;
import midend.llvm.instr.BranchInstr;
import midend.llvm.instr.CallInstr;
import midend.llvm.instr.CompareInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;
import midend.symbol.FuncSymbol;
import midend.symbol.SymbolManager;

import java.util.ArrayList;

public class VisitorExp {
    public static IrValue visitExp(Exp exp) {
        if (exp.canCompute()) {
            return new IrConstInt(exp.getComputationResult());
        }

        return visitAddExp(exp.getAddExp());
    }

    private static IrValue visitAddExp(AddExp addExp) {
        if (addExp.canCompute()) {
            return new IrConstInt(addExp.getComputationResult());
        }

        ArrayList<Node> components = addExp.getComponents();
        // MulExp
        if (components.size() == 1) {
            return visitMulExp((MulExp) components.get(0));
        }
        // AddExp ('+' | '−') MulExp
        else {
            IrValue lValue = visitAddExp((AddExp) components.get(0));
            IrValue rValue = visitMulExp((MulExp) components.get(2));
            String op = ((TokenNode) components.get(1)).getTokenValue();

            return new AluInstr(op, lValue, rValue);
        }
    }

    private static IrValue visitMulExp(MulExp mulExp) {
        if (mulExp.canCompute()) {
            return new IrConstInt(mulExp.getComputationResult());
        }

        ArrayList<Node> components = mulExp.getComponents();
        // UnaryExp
        if (components.size() == 1) {
            return visitUnaryExp((UnaryExp) components.get(0));
        }
        // MulExp ('*' | '/' | '%') UnaryExp
        else {
            IrValue lValue = visitMulExp((MulExp) components.get(0));
            IrValue rValue = visitUnaryExp((UnaryExp) components.get(2));
            String op = ((TokenNode) components.get(1)).getTokenValue();

            return new AluInstr(op, lValue, rValue);
        }
    }

    private static IrValue visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.canCompute()) {
            return new IrConstInt(unaryExp.getComputationResult());
        }

        int componentCount = unaryExp.getComponents().size();
        // PrimaryExp
        if (componentCount == 1) {
            return visitPrimaryExp(unaryExp.getPrimaryExp());
        }
        // UnaryOp UnaryExp
        else if (componentCount == 2) {
            IrValue lValue = new IrConstInt(0);
            IrValue rValue = visitUnaryExp(unaryExp.getUnaryExp());
            String op = unaryExp.getUnaryOp();

            switch (op) {
                case "+":
                    return rValue;
                case "-":
                    return new AluInstr(op, lValue, rValue);
                // TODO:逻辑非
                default:
                    throw new RuntimeException("[ERROR] Invalid Op");
            }
        }
        // Ident '(' [FuncRParams] ')'
        else {
            // 找到函数调用对应的 irFunc
            String funcName = unaryExp.getIdentName();
            FuncSymbol funcSymbol = (FuncSymbol) SymbolManager.getSymbol(funcName);
            if (funcSymbol == null) {
                throw new RuntimeException("[ERROR] Can't find funcSymbol");
            }
            IrFunc irFunc = (IrFunc) funcSymbol.getIrValue();
            // 获取所有实参
            ArrayList<Exp> params = unaryExp.getParams();
            ArrayList<IrValue> paramValues = new ArrayList<>();
            for (Exp param : params) {
                paramValues.add(visitExp(param));
            }

            return new CallInstr(irFunc, paramValues);
        }
    }

    private static IrValue visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.canCompute()) {
            return new IrConstInt(primaryExp.getComputationResult());
        }

        ArrayList<Node> components = primaryExp.getComponents();
        // LVal
        if (components.size() == 1) {
            return VisitorLVal.visitLVal((LVal) components.get(0), false);
        }
        // '(' Exp ')'
        else {
            return visitExp((Exp) components.get(1));
        }
    }

    public static void visitCond(Cond cond, IrBasicBlock trueBlock, IrBasicBlock falseBlock) {
        visitLOrExp(cond.getLOrExp(), trueBlock, falseBlock);
    }

    private static void visitLOrExp(LOrExp lOrExp, IrBasicBlock trueBlock, IrBasicBlock falseBlock) {
        // lAndExp1 || lAndExp2 || ...
        ArrayList<LAndExp> lAndExps = lOrExp.getLAndExps(); // 获取到 lOrExp 的所有 lAndExp
        for (int i = 0; i < lAndExps.size() - 1; i++) {
            IrBasicBlock nextAndBlock = IrBuilder.createIrBasicBlock();  // lAndExp 的下一个 lAndExp
            IrValue lAndValue = visitLAndExp(lAndExps.get(i), trueBlock, nextAndBlock);
            // new BranchInstr(lAndValue, trueBlock, nextAndBlock);
            IrBuilder.setCurrentIrBasicBlock(nextAndBlock);
        }
        visitLAndExp(lAndExps.get(lAndExps.size() - 1), trueBlock, falseBlock);
    }

    private static IrValue visitLAndExp(LAndExp lAndExp, IrBasicBlock trueBlock, IrBasicBlock falseBlock) {
        // eqExp1 && eqExp2 && ...
        ArrayList<EqExp> eqExps = lAndExp.getEqExps();
        for (int i = 0; i < eqExps.size() - 1; i++) {
            IrBasicBlock nextEqBlock = IrBuilder.createIrBasicBlock();
            IrValue eqValue = visitEqExp(eqExps.get(i));
            // 短路求值（若为真，则进入下一个 EqBlock，否则直接退出，跳到 falseBlock）
            new BranchInstr(eqValue, nextEqBlock, falseBlock);
            IrBuilder.setCurrentIrBasicBlock(nextEqBlock);
        }
        IrValue eqValue = visitEqExp(eqExps.get(eqExps.size() - 1));
        new BranchInstr(eqValue, trueBlock, falseBlock);
        return eqValue;
    }

    private static IrValue visitEqExp(EqExp eqExp) {
        // relExp1 ('==' | '!=') relExp2 ('==' | '!=') ...
        ArrayList<RelExp> relExps = eqExp.getRelExps();
        ArrayList<String> relOps = eqExp.getRelOps();

        IrValue lValue = visitRelExp(relExps.get(0));
        IrValue rValue;
        for (int i = 1; i < relExps.size(); i++) {
            rValue = visitRelExp(relExps.get(i));
            lValue = new CompareInstr(relOps.get(i - 1), lValue, rValue);
        }
        lValue = new CompareInstr("!=", lValue, new IrConstInt(0)); // 最后获取eqExp的真值
        return lValue;
    }

    private static IrValue visitRelExp(RelExp relExp) {
        // addExp1 ('<' | '>' | '<=' | '>=') addExp2 ('<' | '>' | '<=' | '>=') ...
        ArrayList<AddExp> addExps = relExp.getAddExps();
        ArrayList<String> relOps = relExp.getRelOps();

        IrValue lValue = visitAddExp(addExps.get(0));
        IrValue rValue;
        for (int i = 1; i < addExps.size(); i++) {
            rValue = visitAddExp(addExps.get(i));
            lValue = new CompareInstr(relOps.get(i - 1), lValue, rValue);
        }
        return lValue;
    }
}
