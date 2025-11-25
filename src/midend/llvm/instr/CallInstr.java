package midend.llvm.instr;

import midend.llvm.IrBuilder;
import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;

import java.util.ArrayList;

public class CallInstr extends IrInstr {
    public CallInstr(IrFunc func, ArrayList<IrValue> params) {
        super(IrValueType.CALL_INSTR, new IrBaseType(func.getIrBaseTypeValue()),
                // 若是 void 函数，直接调用（call ...），否则要有变量接收（%n = call ...）
                func.getIrBaseTypeValue().equals(IrBaseType.TypeValue.VOID) ? "call" : IrBuilder.getLocalVarName()
        );
        addUsee(func);
        params.forEach(this::addUsee);
    }

    private IrFunc getFunc() {
        return (IrFunc) usees.get(0);
    }

    private ArrayList<IrValue> getParams() {
        return new ArrayList<>(usees.subList(1, usees.size()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 非 void 调用要有值
        if (!name.equals("call")) {
            sb.append(name).append(" = ");
        }

        IrFunc func = getFunc();
        sb.append("call ").append(func.getReturnTypeString()).append(" ").append(func.getName());
        sb.append("(");
        // 参数
        ArrayList<IrValue> params = getParams();
        ArrayList<String> paramStrings = new ArrayList<>();
        for (IrValue param : params) {
            paramStrings.add(param.getIrBaseType() + " " + param.getName());
        }
        sb.append(String.join(", ", paramStrings));

        sb.append(")");
        return sb.toString();
    }
}
