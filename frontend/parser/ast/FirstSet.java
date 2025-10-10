package frontend.parser.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirstSet {
    public static final Map<SyntaxType, Set<String>> FIRST_SETS = new HashMap<>();

    public static void initialize() {
        FIRST_SETS.put(SyntaxType.CONST_DECL, new HashSet<>(List.of("const")));
        FIRST_SETS.put(SyntaxType.VAR_DECL, new HashSet<>(List.of("static", "int")));
        FIRST_SETS.put(SyntaxType.DECL, new HashSet<>(List.of("static", "int", "const")));
    }

}
