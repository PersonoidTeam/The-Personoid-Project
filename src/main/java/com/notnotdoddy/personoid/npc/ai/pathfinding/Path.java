package com.notnotdoddy.personoid.npc.ai.pathfinding;

import org.bukkit.Location;

import java.util.List;

public record Path(List<PathNode> nodes) {
    public PathNode getNode(int index) {
        return nodes.get(index);
    }

    public Path merge(Path other) {
        nodes.addAll(other.nodes());
        return this;
    }

    public Path append(PathNode node) {
        nodes.add(node);
        return this;
    }

    public void clean() {
        if (nodes.size() >= 2){
            nodes.remove(0);
        }
    }

    public Location getStartLocation() {
        return nodes.get(0).getLocation().add(0.5F, 0F, 0.5F);
    }

    public Location getEndLocation() {
        return nodes.get(nodes.size() - 1).getLocation().add(0.5F, 0F, 0.5F);
    }
}
