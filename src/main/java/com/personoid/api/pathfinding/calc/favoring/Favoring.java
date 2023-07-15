package com.personoid.api.pathfinding.calc.favoring;

import com.personoid.api.pathfinding.calc.Path;
import com.personoid.api.pathfinding.calc.avoidance.Avoidance;
import com.personoid.api.pathfinding.calc.node.Node;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.bukkit.World;

public class Favoring {
    private static final double BACKTRACKING_COEFFICIENT = 0.5;
    private static Favoring defaultFavoring;

    private final Long2DoubleOpenHashMap map;
    private final World world;

    public Favoring(World world) {
        this.world = world;
        this.map = new Long2DoubleOpenHashMap(512);
        map.defaultReturnValue(1);
        applyAvoidance(world);
    }

    public void applyBacktracking(Path path) {
        for (int i = 0; i < path.size(); i++) {
            Node node = path.getNode(i);
            setCoefficient(node, BACKTRACKING_COEFFICIENT);
        }
    }

    public void applyAvoidance(World world) {
        for (Avoidance avoidance : Avoidance.list(world)) {
            avoidance.apply(map);
        }
    }

    public double getCoefficient(Node node) {
        return map.get(node.getPos().asLong());
    }

    public void setCoefficient(Node node, double coefficient) {
        map.put(node.getPos().asLong(), coefficient);
    }
}
