package com.personoid.api.pathfindingold.favouring;

import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.PathSegment;
import com.personoid.api.pathfindingold.avoidance.Avoidance;
import org.bukkit.World;

import java.util.HashMap;

public class Favouring {
    private static final double BACKTRACKING_COEFFICIENT = 0.5;

    private final HashMap<BlockPos, Double> map = new HashMap<>();
    private final World world;

    public Favouring(World world, PathSegment segment) {
        this(world);

        if (segment != null) {
            applyBacktracking(segment);
        }
    }

    public Favouring(World world) {
        this.world = world;
        applyAvoidance();
    }

    public void applyBacktracking(PathSegment segment) {
        for (int i = 0; i < segment.length(); i++) {
            Node node = segment.getNode(i);
            setCoefficient(node, BACKTRACKING_COEFFICIENT);
        }
    }

    public void applyAvoidance() {
        for (Avoidance avoidance : Avoidance.list(world)) {
            avoidance.apply(map);
        }
    }

    public double getCoefficient(Node node) {
        int x = node.getX();
        int y = node.getY();
        int z = node.getZ();
        return getCoefficient(x, y, z);
    }

    public double getCoefficient(int x, int y, int z) {
        return map.get(new BlockPos(x, y, z));
    }

    public void setCoefficient(Node node, double d) {
        int x = node.getX();
        int y = node.getY();
        int z = node.getZ();
        setCoefficient(x, y, z, d);
    }

    public void setCoefficient(int x, int y, int z, double d) {
        map.put(new BlockPos(x, y, z), d);
    }
}
