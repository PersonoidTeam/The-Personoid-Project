package com.notnotdoddy.personoid.npc.ai.pathfinding;

import com.notnotdoddy.personoid.npc.ai.pathfinding.requirements.PathRequirement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PathNode implements Comparable<PathNode> {
    private final Pathfinder pathfinder;
    private final int x, y, z;
    private double G, F, H;

    public PathNode(Pathfinder pathfinder, int x, int y, int z) {
        this.pathfinder = pathfinder;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PathNode otherNode)) {
            return false;
        }
        return otherNode.x == x && otherNode.y == y && otherNode.z == z;
    }

    public List<PathNode> getNeighbors() {
        return new ArrayList<>() {
            {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block block = getLocation().add(x, y, z).getBlock();
                            if (pathfinder.closedBlocks.contains(block)) continue;
                            PathNode neighbor = new PathNode(pathfinder, PathNode.this.x + x, PathNode.this.y + y, PathNode.this.z + z);
                            if (pathfinder.getRequirements().size() > 0) {
                                for (PathRequirement requirement : pathfinder.getRequirements()) {
                                    if (!requirement.canPathTo(getLocation().getBlock(), block)) {
                                        pathfinder.closedBlocks.add(block);
                                        continue;
                                    }
                                    add(neighbor);
                                }
                            } else {
                                add(neighbor);
                            }
                        }
                    }
                }
            }
        };
    }

    public double distance(PathNode other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2) + Math.pow(z - other.z, 2));
    }

    public Location getLocation() {
        return new Location(pathfinder.getNPC().getLocation().getWorld(), x, y, z).add(0.5F, 0F, 0.5F);
    }

    //region getters and setters

    public double getF() {
        return F;
    }

    public void setF(double f) {
        this.F = f;
    }

    public double getG() {
        return G;
    }

    public void setG(double g) {
        this.G = g;
    }

    public double getH() {
        return H;
    }

    public void setH(double h) {
        this.H = h;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int compareTo(@NotNull PathNode other) {
        return Double.compare(getH(), other.getH());
    }

    //endregion
}
