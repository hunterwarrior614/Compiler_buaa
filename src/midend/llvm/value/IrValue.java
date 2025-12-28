package midend.llvm.value;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;
import midend.llvm.use.IrUse;
import midend.llvm.use.IrUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class IrValue {
    protected final IrValueType irValueType;
    protected final IrBaseType irBaseType;
    protected final String name;
    private final ArrayList<IrUse> useList = new java.util.ArrayList<>();

    public IrValue(IrValueType irValueType, IrBaseType irBaseType, String name) {
        this.irValueType = irValueType;
        this.irBaseType = irBaseType;
        this.name = name;
    }

    public void addUse(IrUse use) {
        useList.add(use);
    }


    public ArrayList<IrUse> getUseList() {
        return useList;
    }

    public void replaceAllUsesWith(IrValue newValue) {
        ArrayList<IrUser> userList = this.useList.stream().map(IrUse::getUser).
                collect(Collectors.toCollection(ArrayList::new));
        for (IrUser user : userList) {
            user.replaceUsee(this, newValue);
            this.deleteUser(user);
            newValue.addUse(new IrUse(user, newValue));
        }
    }

    public void deleteUser(IrUser user) {
        Iterator<IrUse> iterator = useList.iterator();
        while (iterator.hasNext()) {
            IrUse use = iterator.next();
            if (use.getUser() == user) {
                iterator.remove();
                return;
            }
        }
    }

    public String getName() {
        return name;
    }

    public IrValueType getIrValueType() {
        return irValueType;
    }

    public IrBaseType getIrBaseType() {
        return irBaseType;
    }

    public IrBaseType.TypeValue getIrBaseTypeValue() {
        return irBaseType.getTypeValue();
    }

    // Mips
    public String getOriginName() {
        return name.substring(1);
    }
}
