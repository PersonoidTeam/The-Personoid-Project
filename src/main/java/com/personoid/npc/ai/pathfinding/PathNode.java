package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.ai.pathfinding.requirements.PathRequirement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PathNode implements Comparable<PathNode> {
    private final Pathfinder pathfinder;
    int x, y, z;
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

    public ArrayList<PathNode> getNeighbours() {
        return new ArrayList<>() {{
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block block = getLocation().clone().add(x, y, z).getBlock();
                        PathNode neighbor = new PathNode(pathfinder, PathNode.this.x + x, PathNode.this.y + y, PathNode.this.z + z);
                        if (pathfinder.getRequirements().size() > 0) {
                            for (PathRequirement requirement : pathfinder.getRequirements()) {
                                if (!requirement.canPathTo(getLocation().getBlock(), block)) continue;
                                add(neighbor);
                            }
                        } else add(neighbor);
                    }
                }
            }
        }};
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

    public PathNode cloneAndMove(int x, int y, int z) {
        PathNode var3 = new PathNode(pathfinder, x, y, z);
        var3.g = (float) this.g;
        var3.h = (float) this.h;
        var3.f = (float) this.f;
        return var3;
    }
}
