package frontend.parser.ast;

import java.util.ArrayList;

public abstract class Node {
    private final ArrayList<Node> components;


    public Node() {
        components = new ArrayList<>();
    }

    public abstract void Parse();
}
