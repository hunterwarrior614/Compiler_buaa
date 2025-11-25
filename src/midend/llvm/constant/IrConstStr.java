package midend.llvm.constant;

import midend.llvm.type.IrBaseType;
import midend.llvm.type.IrValueType;

public class IrConstStr extends IrConst {
    private final String content;

    public IrConstStr(String name, String content) {
        super(IrValueType.CONST_DATA, new IrBaseType(IrBaseType.TypeValue.STRING, getLength(content)), name);
        this.content = content;
    }

    private static int getLength(String content) {
        int length = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\\') {
                length++;
                i++;    // '\n' 算作一个
            } else {
                length++;
            }
        }
        return length + 1;  // 结尾的 '\0'
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" = private unnamed_addr constant ");
        sb.append(getIrBaseType());
        sb.append(" c\"");
        sb.append(content.replaceAll("\\\\n", "\\\\0A"));    // "\\\\n"经转义后得到"\\n"，再经转义匹配字符串中"\n"
        sb.append("\\00\"");
        return sb.toString();
    }
}
