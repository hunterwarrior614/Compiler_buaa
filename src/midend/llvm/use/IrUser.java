package midend.llvm.use;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.value.IrValue;

import java.util.ArrayList;

public class IrUser extends IrValue {
    protected final ArrayList<IrValue> usees;

    public IrUser(IrValueType irValueType, IrBaseType irBaseType, String name) {
        super(irValueType, irBaseType, name);
        usees = new ArrayList<>();
    }

    public void addUsee(IrValue usee) {
        usees.add(usee);
        if (usee != null) {
            usee.addUse(new IrUse(this, usee));
        }
    }

    public ArrayList<IrValue> getUsees() {
        return usees;
    }

    public void replaceUsee(IrValue oldUsee, IrValue newUsee) {
        // 将newValue加入到oldValue中，位置不变
        int index = this.usees.indexOf(oldUsee);
        this.usees.set(index, newUsee);
    }
}
