package midend.visit;

import frontend.parser.ast.CompUnit;
import frontend.parser.ast.decl.Decl;
import frontend.parser.ast.def.FuncDef;
import frontend.parser.ast.def.MainFuncDef;

import java.util.ArrayList;

public class VisitorCompUnit {
    private final CompUnit compUnit;

    public VisitorCompUnit(CompUnit compUnit) {
        this.compUnit = compUnit;
    }

    public void visitCompUnit() {
        ArrayList<Decl> decls = compUnit.getDecls();
        ArrayList<FuncDef> funcDefs = compUnit.getFuncDefs();
        MainFuncDef mainFuncDef = compUnit.getMainFuncDef();

        for (Decl decl : decls) {
            VisitorDecl.visitDecl(decl);
        }
        for (FuncDef funcDef : funcDefs) {
            VisitorFuncDef.visitComFuncDef(funcDef);
        }
        VisitorFuncDef.visitMainFuncDef(mainFuncDef);
    }
}
