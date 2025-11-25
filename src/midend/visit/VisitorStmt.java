package midend.visit;

import frontend.parser.ast.exp.Exp;
import frontend.parser.ast.stmt.Block;
import frontend.parser.ast.stmt.Cond;
import frontend.parser.ast.stmt.ForStmt;
import frontend.parser.ast.stmt.Stmt;
import frontend.parser.ast.val.LVal;
import midend.llvm.IrBuilder;
import midend.llvm.constant.IrConstStr;
import midend.llvm.instr.ReturnInstr;
import midend.llvm.instr.StoreInstr;
import midend.llvm.instr.io.GetIntInstr;
import midend.llvm.instr.JumpInstr;
import midend.llvm.instr.io.PrintIntInstr;
import midend.llvm.instr.io.PrintStrInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrLoop;
import midend.llvm.value.IrValue;
import midend.symbol.SymbolManager;

import java.util.ArrayList;

public class VisitorStmt {
    public static void visitStmt(Stmt stmt) {
        switch (stmt.getStmtType()) {
            case ReturnStmt -> visitReturnStmt(stmt);
            case AssignStmt -> visitAssignStmt(stmt);
            case BlockStmt -> visitBlockStmt(stmt);
            case GetIntStmt -> visitGetIntStmt(stmt);
            case PrintStmt -> visitPrintStmt(stmt);
            case ExpStmt -> visitExpStmt(stmt);
            case IfStmt -> visitIfStmt(stmt);
            case ForStmt -> visitStmtForStmt(stmt);
            case BreakStmt -> visitBreakStmt();
            case ContinueStmt -> visitContinueStmt();
            default -> throw new RuntimeException("[ERROR] Invalid stmt type");
        }
    }

    private static void visitReturnStmt(Stmt stmt) {
        // 'return' [Exp] ';'
        IrValue irReturn = null;
        if (stmt.hasReturnValue()) {
            irReturn = VisitorExp.visitExp(stmt.getReturnExp());
        }
        new ReturnInstr(irReturn);
    }

    private static void visitAssignStmt(Stmt stmt) {
        // LVal '=' Exp ';'
        LVal lVal = stmt.getAssignLVal();
        Exp exp = stmt.getAssignExp();

        IrValue lValue = VisitorLVal.visitLVal(lVal, true);
        IrValue rValue = VisitorExp.visitExp(exp);

        new StoreInstr(rValue, lValue);
    }

    private static void visitBlockStmt(Stmt stmt) {
        // Block
        SymbolManager.goToSonSymbolTable();

        Block block = stmt.getBlock();
        VisitorBlock.visitBlock(block);

        SymbolManager.goBackToParentSymbolTable();
    }

    private static void visitGetIntStmt(Stmt stmt) {
        // LVal '=' 'getint()' ';'
        LVal lVal = stmt.getAssignLVal();
        IrValue lValue = VisitorLVal.visitLVal(lVal, true);
        // 先获得getint的值
        GetIntInstr getIntInstr = new GetIntInstr();
        // 再将值赋给lValue
        new StoreInstr(getIntInstr, lValue);
    }

    private static void visitPrintStmt(Stmt stmt) {
        String formatString = stmt.getPrintfFStr();
        ArrayList<Exp> exps = stmt.getPrintfExpList();

        ArrayList<IrValue> printValues = new ArrayList<>();
        for (Exp exp : exps) {
            printValues.add(VisitorExp.visitExp(exp));
        }
        int printValueCount = 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < formatString.length() - 1; i++) {   // 删去头尾的引号
            // 格式输出
            if (formatString.charAt(i) == '%' && formatString.charAt(i + 1) == 'd') {
                // 先将之前的字符串都输出
                if (!sb.isEmpty()) {
                    IrConstStr irConstStr = IrBuilder.createIrConstStr(sb.toString());
                    new PrintStrInstr(irConstStr);
                    sb.setLength(0);
                }
                // 再输出整数
                IrValue printValue = printValues.get(printValueCount++);
                new PrintIntInstr(printValue);

                i++;
            }
            // TODO:%%要处理吗？
            // 转义字符（尽可能为\n）
            else if (formatString.charAt(i) == '\\') {
                sb.append("\\n");
                i++;
            } else {
                sb.append(formatString.charAt(i));
            }
        }
        // 将sb中的字符串都输出
        if (!sb.isEmpty()) {
            IrConstStr irConstStr = IrBuilder.createIrConstStr(sb.toString());
            new PrintStrInstr(irConstStr);
        }
    }

    private static void visitExpStmt(Stmt stmt) {
        if (stmt.getExp() != null) {
            VisitorExp.visitExp(stmt.getExp());
        }
    }

    private static void visitIfStmt(Stmt stmt) {
        // 'if' '(' Cond ')' Stmt1 [ 'else' Stmt2 ]
        Cond cond = stmt.getIfCond();
        Stmt stmt1 = stmt.getIfStmt();
        IrBasicBlock ifBlock = IrBuilder.createIrBasicBlock();

        // 有 else
        if (stmt.hasElseStmt()) {
            IrBasicBlock elseBlock = IrBuilder.createIrBasicBlock();

            VisitorExp.visitCond(cond, ifBlock, elseBlock); // 解析 cond

            IrBuilder.setCurrentIrBasicBlock(ifBlock);
            VisitorStmt.visitStmt(stmt1);    // 解析 stmt1

            IrBasicBlock followBlock = IrBuilder.createIrBasicBlock();
            new JumpInstr(followBlock); // 跳转到if-else语句后的基本块

            IrBuilder.setCurrentIrBasicBlock(elseBlock);
            VisitorStmt.visitStmt(stmt.getElseStmt());  // 解析 stmt2
            new JumpInstr(followBlock);

            IrBuilder.setCurrentIrBasicBlock(followBlock);
        }
        // 没有 else
        else {
            IrBasicBlock followBlock = IrBuilder.createIrBasicBlock();

            VisitorExp.visitCond(cond, ifBlock, followBlock); // 解析 cond

            IrBuilder.setCurrentIrBasicBlock(ifBlock);
            VisitorStmt.visitStmt(stmt1);    // 解析 stmt1
            new JumpInstr(followBlock);

            IrBuilder.setCurrentIrBasicBlock(followBlock);
        }
    }

    private static void visitStmtForStmt(Stmt stmt) {
        // 'for' '(' [init] ';' [Cond] ';' [step] ')' Stmt
        IrBasicBlock condBlock = IrBuilder.createIrBasicBlock();
        IrBasicBlock stepBlock = IrBuilder.createIrBasicBlock();
        IrBasicBlock bodyBlock = IrBuilder.createIrBasicBlock();
        IrBasicBlock followBlock = IrBuilder.createIrBasicBlock();

        IrBuilder.pushLoop(new IrLoop(stepBlock, followBlock)); // 将该for循环入栈（用于处理嵌套循环）

        // 解析初始化ForStmt
        ForStmt initStmt = stmt.getInitForStmt();
        if (initStmt != null) {
            visitForStmt(initStmt);
        }
        new JumpInstr(condBlock);   // 跳转到Cond判断

        // 解析Cond
        IrBuilder.setCurrentIrBasicBlock(condBlock);
        Cond cond = stmt.getForCond();
        if (cond != null) {
            VisitorExp.visitCond(cond, bodyBlock, followBlock);
        }
        new JumpInstr(bodyBlock);

        // 解析for循环体
        IrBuilder.setCurrentIrBasicBlock(bodyBlock);
        Stmt forBody = stmt.getForBody();
        visitStmt(forBody);
        new JumpInstr(stepBlock);   // 循环体执行完跳转到step

        // 解析step
        IrBuilder.setCurrentIrBasicBlock(stepBlock);
        ForStmt stepStmt = stmt.getStepForStmt();
        if (stepStmt != null) {
            visitForStmt(stepStmt);
        }
        new JumpInstr(condBlock);   // step执行完跳转到Cond判断

        IrBuilder.popLoop();    // 将该for循环出栈

        IrBuilder.setCurrentIrBasicBlock(followBlock);
    }

    private static void visitForStmt(ForStmt forStmt) {
        // LVal '=' Exp { ',' LVal '=' Exp }
        ArrayList<LVal> lVals = forStmt.getLVals();
        ArrayList<Exp> exps = forStmt.getExps();

        for (int i = 0; i < lVals.size(); i++) {
            IrValue lValue = VisitorLVal.visitLVal(lVals.get(i), true);
            IrValue rValue = VisitorExp.visitExp(exps.get(i));

            new StoreInstr(rValue, lValue);
        }
    }

    private static void visitBreakStmt() {
        // 跳转到当前循环体的followBlock
        new JumpInstr(IrBuilder.getCurrentLoop().getFollowBlock());
    }

    private static void visitContinueStmt() {
        // 跳转到当前循环体的stepBlock
        new JumpInstr(IrBuilder.getCurrentLoop().getStepBlock());
    }
}
