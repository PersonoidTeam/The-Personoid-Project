package com.personoid.api.pathfindingold;

public class PathSegment {
    private final Node[] nodes;
    private int index;

    public PathSegment(Node[] nodes) {
        this.nodes = nodes;
    }

    public Node getNode(int index) {
        return nodes[index];
    }

    public Node getCurrentNode() {
        return getNode(index);
    }

    public Node getFinalNode() {
        return getNode(nodes.length - 1);
    }

    public boolean isEmpty() {
        return index >= nodes.length;
    }

    public void next() {
        index++;
    }

    public int length() {
        return nodes.length;
    }

    public int getPointer() {
        return index;
    }

    public boolean isFinished() {
        return index >= nodes.length - 1;
    }
}
