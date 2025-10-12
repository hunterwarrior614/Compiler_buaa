package frontend.parser.ast;

public enum SyntaxType {
    COMP_UNIT("CompUnit"),
    DECL("Decl"),
    FUNC_DEF("FuncDef"),
    MAIN_FUNC_DEF("MainFuncDef"),
    CONST_DECL("ConstDecl"),
    VAR_DECL("VarDecl"),
    FUNC_TYPE("FuncType"),
    BTYPE("BType"),
    CONST_DEF("ConstDef"),
    TOKEN("Token"),
    CONST_EXP("ConstExp"),
    CONST_INIT_VAL("ConstInitVal"),
    ADD_EXP("AddExp"),
    MUL_EXP("MulExp"),
    UNARY_EXP("UnaryExp"),
    PRIMARY_EXP("PrimaryExp"),
    UNARY_OP("UnaryOp"),
    FUNC_REAL_PARAMS("FuncRParams"),
    EXP("Exp"),
    LVAL("LVal"),
    VAR_DEF("VarDef"),
    INIT_VAL("InitVal"),
    FUNC_FORMAL_PARAMS("FuncFParams"),
    FUNC_FORMAL_PARAM("FuncFParam"),
    BLOCK("Block"),
    BLOCK_ITEM("BlockItem"),
    STATEMENT("Stmt"),
    CONDITION("Cond"),
    FOR_STATEMENT("ForStmt"),
    LOR_EXP("LOrExp"),
    LAND_EXP("LAndExp"),
    EQ_EXP("EqExp"),
    REL_EXP("RelExp"),
    NUMBER("Number"),
    ;

    private final String typeName;

    SyntaxType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
