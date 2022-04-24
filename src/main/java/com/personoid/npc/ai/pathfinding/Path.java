package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.NPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {
    public net.minecraft.world.level.pathfinder.Path path;
    private List<PathNode> nodes;
    private int nextNodeIndex = 0;

    public Path(net.minecraft.world.level.pathfinder.Path path) {
        this.path = path;
    }

    public Path(List<PathNode> nodes) {
        this.nodes = nodes;
    }

    public Path(PathNode... nodes) {
        this.nodes = new ArrayList<>(Arrays.stream(nodes).toList());
    }

    public List<PathNode> getNodes() {
        return nodes;
    }

    public Path append(PathNode... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public Path append(List<PathNode> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public int getNextNodeIndex() {
        return nextNodeIndex;
    }

    public PathNode getStart() {
        return nodes.get(0);
    }

    public PathNode getEnd() {
        return nodes.get(nodes.size() - 1);
    }

    public PathNode getNode(int index) {
        return nodes.get(index);
    }

    public int size() {
        return nodes.size();
    }

/*    public Vec3 getNPCPosAtNode(NPC npc, int index) {
        // FIXME: hacky workaround while I figure out what's going on :(
        if (index >= this.nodes.size()) return new Vec3(npc.getLocation().getX(), npc.getLocation().getY(), npc.getLocation().getZ());

        PathNode node = this.nodes.get(index);
        double x = (double)node.x + (double)((int)(npc.getBbWidth() + 1.0F)) * 0.5D;
        double y = (double)node.z + (double)((int)(npc.getBbWidth() + 1.0F)) * 0.5D;
        return new Vec3(x, node.y, y);
    }*/

    public Vec3 getNPCPosAtNode(NPC npc, int index) {
        try {
            PathNode node = nodes.get(index);
            double x = (double)node.x + (double)((int)(npc.getBbWidth() + 1.0F)) * 0.5D;
            double y = (double)node.z + (double)((int)(npc.getBbWidth() + 1.0F)) * 0.5D;
            return new Vec3(x, node.y, y);
        } catch (IndexOutOfBoundsException e) {
            return new Vec3(npc.getLocation().getX(), npc.getLocation().getY(), npc.getLocation().getZ());
        }
    }

    public Vec3 getNextNPCPos(NPC npc) {
        return this.getNPCPosAtNode(npc, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return getNodePos(nextNodeIndex);
    }

    public BlockPos getNodePos(int index) {
        PathNode node = nodes.get(index);
        return new BlockPos(node.x, node.y, node.z);
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    public void replaceNode(int index, PathNode node) {
        this.nodes.set(index, node);
    }
}
