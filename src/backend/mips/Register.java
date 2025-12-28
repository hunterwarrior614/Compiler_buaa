package backend.mips;

import java.util.ArrayList;

public enum Register {
    ZERO,

    AT,

    V0, V1, // 返回值寄存器

    A0, A1, A2, A3, // 参数寄存器

    T0, T1, T2, T3, T4, T5, T6, T7, // 临时寄存器
    T8, T9,

    S0, S1, S2, S3, S4, S5, S6, S7, // 全局寄存器

    K0, K1, // 系统保留寄存器

    GP, // 全局指针

    SP, FP, // 栈帧寄存器

    RA; // 返回地址

    public static Register getRegister(int registerIndex) {
        return Register.values()[registerIndex];
    }

    public static ArrayList<Register> getUsAbleRegisters() {
        ArrayList<Register> usableRegisters = new ArrayList<>();

        usableRegisters.add(T0);
        usableRegisters.add(T1);
        usableRegisters.add(T2);
        usableRegisters.add(T3);
        usableRegisters.add(T4);
        usableRegisters.add(T5);
        usableRegisters.add(T6);
        usableRegisters.add(T7);
        usableRegisters.add(T8);
        usableRegisters.add(T9);

        usableRegisters.add(S0);
        usableRegisters.add(S1);
        usableRegisters.add(S2);
        usableRegisters.add(S3);
        usableRegisters.add(S4);
        usableRegisters.add(S5);
        usableRegisters.add(S6);
        usableRegisters.add(S7);

        return usableRegisters;
    }

    @Override
    public String toString() {
        return "$" + name().toLowerCase();
    }
}
