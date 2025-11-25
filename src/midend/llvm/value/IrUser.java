package midend.llvm.value;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

import java.util.ArrayList;

public class IrUser extends IrValue {
    protected final ArrayList<IrValue> usees;

    public IrUser(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
        usees = new ArrayList<>();
    }

    public void addUsee(IrValue usee) {
        usees.add(usee);
    }

}
