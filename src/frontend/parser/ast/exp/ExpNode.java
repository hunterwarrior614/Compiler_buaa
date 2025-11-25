package frontend.parser.ast.exp;

import frontend.parser.ast.Node;
import frontend.parser.ast.SyntaxType;

public abstract class ExpNode extends Node {
    protected int canCompute;   // -1 表示不确定，0 表示无法编译时计算，1 表示编译时可计算
    protected int computationResult;
    protected boolean validResult = false;

    public ExpNode(SyntaxType type) {
        super(type);
        canCompute = -1;
    }

    public abstract boolean canCompute();

    public abstract int getComputationResult();
}
