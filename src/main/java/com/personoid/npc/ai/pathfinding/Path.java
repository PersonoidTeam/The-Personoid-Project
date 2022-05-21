package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.NPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class Path {
    private final Node[] nodes;
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

    public Vec3 getNPCPosAtNode(NPC npc, int index) {
        try {
            Node node = getNode(index);
            double x = node.getX() + (double)((int)(npc.getBbWidth() + 1F)) * 0.5D;
            double y = node.getZ() + (double)((int)(npc.getBbWidth() + 1F)) * 0.5D;
            return new Vec3(x, node.getY(), y);
        } catch (IndexOutOfBoundsException e) {
            return new Vec3(npc.getX(), npc.getY(), npc.getZ());
        }
    }

    public int getNextNodeIndex() {
        return nextNodeIndex;
    }

    public Vec3 getNextNPCPos(NPC npc) {
        return this.getNPCPosAtNode(npc, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return getNodePos(nextNodeIndex);
    }

    public BlockPos getNodePos(int index) {
        Node node = getNode(index);
        return new BlockPos(node.getX(), node.getY(), node.getZ());
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
}
