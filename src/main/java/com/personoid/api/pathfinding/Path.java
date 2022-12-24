package com.personoid.api.pathfinding;

import com.personoid.api.npc.NPC;
import org.bukkit.util.Vector;

public class Path {
    private Node[] nodes;
    private int nextNodeIndex;

    public Path(Node[] nodes) {
        this.nodes = nodes;
    }

    public Node getNode(int index) {
        return nodes[index];
    }

    public Node[] getNodes() {
        return nodes;
    }

    public int size() {
        return nodes.length;
    }

    public Vector getNPCPosAtNode(NPC npc, int index) {
        try {
            Node node = getNode(index);
            double x = node.getX() + 0.5D;
            double y = node.getZ() + 0.5D;
            return new Vector(x, node.getY(), y);
        } catch (IndexOutOfBoundsException e) {
            return npc.getLocation().toVector();
        }
    }

    public int getNextNodeIndex() {
        return nextNodeIndex;
    }

    public Vector getNextNPCPos(NPC npc) {
        return this.getNPCPosAtNode(npc, this.nextNodeIndex);
    }

    public Vector getNextNodePos() {
        return getNodePos(nextNodeIndex);
    }

    public Vector getNodePos(int index) {
        Node node = getNode(index);
        return new Vector(node.getX(), node.getY(), node.getZ());
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.length;
    }

    public void replaceNode(int index, Node node) {
        this.nodes[index] = node;
    }

    public void clean() {
        // remove first node and move all nodes along
        Node[] newNodes = new Node[nodes.length - 1];
        System.arraycopy(nodes, 1, newNodes, 0, newNodes.length);
        nodes = newNodes;
    }
}
