package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.ai.pathfinding.requirements.PathRequirement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PathNode implements Comparable<PathNode> {
    private final Pathfinder pathfinder;
    final int x, y, z;
    double g, f, h;

    public PathNode(Pathfinder pathfinder, Location location) {
        this.pathfinder = pathfinder;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public PathNode(Pathfinder pathfinder, int x, int y, int z) {
        this.pathfinder = pathfinder;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location getLocation() {
        return new Location(pathfinder.getWorld(), x, y, z);
    }

    public List<PathNode> getNeighbours() {
        List<PathNode> nodes = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = getLocation().clone().add(x, y, z).getBlock();
                    PathNode neighbor = new PathNode(pathfinder, this.x + x, this.y + y, this.z + z);
                    if (pathfinder.getRequirements().size() > 0) {
                        for (PathRequirement requirement : pathfinder.getRequirements()) {
                            if (!requirement.canPathTo(getLocation().getBlock(), block)) continue;
                            nodes.add(neighbor);
                        }
                    } else nodes.add(neighbor);
                }
            }
        }
        return nodes;
    }

    public double distanceTo(PathNode node) {
        return Math.sqrt(Math.pow(x - node.x, 2) + Math.pow(y - node.y, 2) + Math.pow(z - node.z, 2));
    }

    @Override
    public int compareTo(@NotNull PathNode node) {
        return Double.compare(h, node.h);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof PathNode node) {
            return node.x == x && node.y == y && node.z == z;
        }
        return false;
    }
}
