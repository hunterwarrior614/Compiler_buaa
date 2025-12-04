package backend.mips.assembly;

public enum MipsType {
    DATA,
    LABEL,
    ANNOTATION,

    JUMP,           // 跳转
    BRANCH,

    SYSCALL,        // 系统调用

    ALU,            // 计算
    MDU,
    COMPARE,

    LSU,

    MARS_PSEUDO,    // MARS 提供的伪指令
}
