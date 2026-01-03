package optimize;

import backend.mips.Register;
import midend.llvm.instr.IrInstr;
import midend.llvm.instr.MoveInstr;
import midend.llvm.value.IrBasicBlock;
import midend.llvm.value.IrFunc;
import midend.llvm.value.IrValue;
import midend.llvm.value.IrParameter;

import java.util.*;

public class RegisterAllocator {
    private final IrFunc func;
    private final List<Register> K_Registers;
    private final int K;

    private class Node {
        IrValue value;
        Set<Node> adjList = new HashSet<>();
        int degree = 0;
        Register color = null;
        Set<Move> moveList = new HashSet<>();
        Node alias = null;
        boolean precolored = false;

        public Node(IrValue value) {
            this.value = value;
        }
    }

    private class Move {
        Node src;
        Node dst;

        public Move(Node src, Node dst) {
            this.src = src;
            this.dst = dst;
        }
    }

    private final Map<IrValue, Node> nodeMap = new HashMap<>();
    private final Set<Node> precolored = new HashSet<>();
    private final Set<Node> initial = new HashSet<>();
    private final Set<Node> simplifyWorklist = new LinkedHashSet<>();
    private final Set<Node> freezeWorklist = new LinkedHashSet<>();
    private final Set<Node> spillWorklist = new LinkedHashSet<>();
    private final Set<Node> coalescedNodes = new LinkedHashSet<>();
    private final Set<Node> coloredNodes = new LinkedHashSet<>();
    private final Stack<Node> selectStack = new Stack<>();

    private final Set<Move> worklistMoves = new LinkedHashSet<>();
    private final Set<Move> activeMoves = new HashSet<>();

    public RegisterAllocator(IrFunc func) {
        this.func = func;
        this.K_Registers = Register.getUsAbleRegisters();
        this.K = K_Registers.size();
    }

    public void run() {
        // Initialize
        nodeMap.clear();
        precolored.clear();
        initial.clear();
        simplifyWorklist.clear();
        freezeWorklist.clear();
        spillWorklist.clear();
        coalescedNodes.clear();
        coloredNodes.clear();
        selectStack.clear();
        worklistMoves.clear();
        activeMoves.clear();

        // 1. Build
        build();

        // 2. Make Worklist
        makeWorklist();

        // 3. Loop
        while (!simplifyWorklist.isEmpty() || !worklistMoves.isEmpty() || !freezeWorklist.isEmpty()
                || !spillWorklist.isEmpty()) {
            if (!simplifyWorklist.isEmpty())
                simplify();
            else if (!worklistMoves.isEmpty())
                coalesce();
            else if (!freezeWorklist.isEmpty())
                freeze();
            else selectSpill();
        }

        // 4. Assign Colors
        assignColors();
    }

    private Node getNode(IrValue val) {
        if (!nodeMap.containsKey(val)) {
            Node node = new Node(val);
            nodeMap.put(val, node);
            if (func.getValueRegisterMap().containsKey(val)) {
                node.precolored = true;
                node.color = func.getValueRegisterMap().get(val);
                node.degree = Integer.MAX_VALUE;
                precolored.add(node);
            } else {
                initial.add(node);
            }
        }
        return nodeMap.get(val);
    }

    private void addEdge(Node u, Node v) {
        if (u == v || u.adjList.contains(v))
            return;
        if (!u.precolored) {
            u.adjList.add(v);
            u.degree++;
        }
        if (!v.precolored) {
            v.adjList.add(u);
            v.degree++;
        }
    }

    private boolean isAllocatable(IrValue val) {
        return !(val instanceof midend.llvm.constant.IrConst) &&
                !(val instanceof midend.llvm.value.IrBasicBlock) &&
                !(val instanceof midend.llvm.value.IrFunc);
    }

    private void build() {
        for (IrParameter param : func.getParams()) {
            getNode(param);
        }
        for (IrBasicBlock block : func.getBasicBlocks()) {
            for (IrInstr instr : block.getInstrs()) {
                if (!instr.getIrBaseType().isVoid()) {
                    getNode(instr);
                }
                for (IrValue use : instr.getUsees()) {
                    if (isAllocatable(use)) {
                        getNode(use);
                    }
                }
            }
        }

        for (IrBasicBlock block : func.getBasicBlocks()) {
            Set<IrValue> live = new HashSet<>(block.getOutValueSet());
            live.removeIf(v -> !isAllocatable(v));

            List<IrInstr> instrs = block.getInstrs();
            for (int i = instrs.size() - 1; i >= 0; i--) {
                IrInstr instr = instrs.get(i);

                if (instr instanceof MoveInstr move) {
                    if (isAllocatable(move.getSrcValue()) && isAllocatable(move.getDstValue())) {
                        Node dst = getNode(move.getDstValue());
                        Node src = getNode(move.getSrcValue());

                        live.remove(move.getDstValue());

                        Move m = new Move(src, dst);
                        worklistMoves.add(m);
                        dst.moveList.add(m);
                        src.moveList.add(m);

                        for (IrValue l : live) {
                            Node n = getNode(l);
                            if (n != src) {
                                addEdge(dst, n);
                            }
                        }
                        live.add(move.getSrcValue());
                    } else if (isAllocatable(move.getDstValue())) {
                        live.remove(move.getDstValue());
                        Node dst = getNode(move.getDstValue());
                        for (IrValue l : live) {
                            addEdge(dst, getNode(l));
                        }
                    }
                } else {
                    if (!instr.getIrBaseType().isVoid()) {
                        live.remove(instr);
                        Node dst = getNode(instr);
                        for (IrValue l : live) {
                            addEdge(dst, getNode(l));
                        }
                    }

                    for (IrValue use : instr.getUsees()) {
                        if (isAllocatable(use)) {
                            live.add(use);
                        }
                    }
                }
            }

            // Handle parameters definition at the entry block
            if (block == func.getBasicBlocks().get(0)) {
                for (IrParameter param : func.getParams()) {
                    if (live.contains(param)) {
                        live.remove(param);
                        Node pNode = getNode(param);
                        for (IrValue l : live) {
                            addEdge(pNode, getNode(l));
                        }
                    }
                }
            }
        }
    }

    private void makeWorklist() {
        Iterator<Node> it = initial.iterator();
        while (it.hasNext()) {
            Node n = it.next();
            it.remove();
            if (n.degree >= K) {
                spillWorklist.add(n);
            } else if (isMoveRelated(n)) {
                freezeWorklist.add(n);
            } else {
                simplifyWorklist.add(n);
            }
        }
    }

    private boolean isMoveRelated(Node n) {
        return !nodeMoves(n).isEmpty();
    }

    private Set<Move> nodeMoves(Node n) {
        Set<Move> moves = new HashSet<>();
        for (Move m : n.moveList) {
            if (activeMoves.contains(m) || worklistMoves.contains(m)) {
                moves.add(m);
            }
        }
        return moves;
    }

    private void simplify() {
        Iterator<Node> it = simplifyWorklist.iterator();
        Node n = it.next();
        it.remove();
        selectStack.push(n);
        for (Node m : getAdjacent(n)) {
            decrementDegree(m);
        }
    }

    private Set<Node> getAdjacent(Node n) {
        Set<Node> adj = new HashSet<>(n.adjList);
        selectStack.forEach(adj::remove);
        adj.removeAll(coalescedNodes);
        return adj;
    }

    private void decrementDegree(Node m) {
        int d = m.degree;
        m.degree--;
        if (d == K) {
            Set<Node> nodes = getAdjacent(m);
            nodes.add(m);
            enableMoves(nodes);
            spillWorklist.remove(m);
            if (isMoveRelated(m)) {
                freezeWorklist.add(m);
            } else {
                simplifyWorklist.add(m);
            }
        }
    }

    private void enableMoves(Set<Node> nodes) {
        for (Node n : nodes) {
            for (Move m : nodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    worklistMoves.add(m);
                }
            }
        }
    }

    private void coalesce() {
        Iterator<Move> it = worklistMoves.iterator();
        Move m = it.next();
        it.remove();

        Node x = getAlias(m.src);
        Node y = getAlias(m.dst);
        Node u, v;
        if (y.precolored) {
            u = y;
            v = x;
        } else {
            u = x;
            v = y;
        }

        if (u == v) {
            addWorkList(u);
        } else if (v.precolored || u.adjList.contains(v)) {
            addWorkList(u);
            addWorkList(v);
        } else if ((u.precolored && checkGeorge(u, v)) || (!u.precolored && checkBriggs(u, v))) {
            combine(u, v);
            addWorkList(u);
        } else {
            activeMoves.add(m);
        }
    }

    private void addWorkList(Node u) {
        if (!u.precolored && !isMoveRelated(u) && u.degree < K) {
            freezeWorklist.remove(u);
            simplifyWorklist.add(u);
        }
    }

    private Node getAlias(Node n) {
        if (coalescedNodes.contains(n)) {
            return getAlias(n.alias);
        }
        return n;
    }

    private boolean checkGeorge(Node u, Node v) {
        for (Node t : getAdjacent(v)) {
            if (!ok(t, u))
                return false;
        }
        return true;
    }

    private boolean checkBriggs(Node u, Node v) {
        Set<Node> adj = new HashSet<>(getAdjacent(u));
        adj.addAll(getAdjacent(v));
        int k = 0;
        for (Node n : adj) {
            if (n.degree >= K)
                k++;
        }
        return k < K;
    }

    private boolean ok(Node t, Node r) {
        return t.degree < K || t.precolored || t.adjList.contains(r);
    }

    private void combine(Node u, Node v) {
        if (freezeWorklist.contains(v)) {
            freezeWorklist.remove(v);
        } else {
            spillWorklist.remove(v);
        }
        coalescedNodes.add(v);
        v.alias = u;
        u.moveList.addAll(v.moveList);
        enableMoves(Collections.singleton(v));

        for (Node t : getAdjacent(v)) {
            addEdge(t, u);
            decrementDegree(t);
        }
        if (u.degree >= K && freezeWorklist.contains(u)) {
            freezeWorklist.remove(u);
            spillWorklist.add(u);
        }
    }

    private void freeze() {
        Iterator<Node> it = freezeWorklist.iterator();
        Node u = it.next();
        it.remove();
        simplifyWorklist.add(u);
        freezeMoves(u);
    }

    private void freezeMoves(Node u) {
        for (Move m : nodeMoves(u)) {
            Node x = m.src;
            Node y = m.dst;
            Node v;
            if (getAlias(y) == getAlias(u)) {
                v = getAlias(x);
            } else {
                v = getAlias(y);
            }
            activeMoves.remove(m);
            if (nodeMoves(v).isEmpty() && v.degree < K) {
                freezeWorklist.remove(v);
                simplifyWorklist.add(v);
            }
        }
    }

    private void selectSpill() {
        Iterator<Node> it = spillWorklist.iterator();
        Node n = it.next();
        it.remove();
        simplifyWorklist.add(n);
        freezeMoves(n);
    }

    private void assignColors() {
        while (!selectStack.isEmpty()) {
            Node n = selectStack.pop();
            Set<Register> okColors = new HashSet<>(K_Registers);

            for (Node w : n.adjList) {
                Node alias = getAlias(w);
                if (coloredNodes.contains(alias) || precolored.contains(alias)) {
                    okColors.remove(alias.color);
                }
            }

            if (!okColors.isEmpty()) {
                coloredNodes.add(n);
                n.color = okColors.iterator().next();
            }
        }

        for (Node n : coalescedNodes) {
            n.color = getAlias(n).color;
        }

        for (Node n : nodeMap.values()) {
            if (n.color != null) {
                func.getValueRegisterMap().put(n.value, n.color);
            }
        }
    }
}
