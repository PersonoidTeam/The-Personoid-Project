package com.personoid.api.pathfindingold.avoidance;

import com.personoid.api.pathfindingold.BlockPos;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Avoidance {
    private final BlockPos pos;
    private final AvoidanceType type;

    public Avoidance(BlockPos pos, AvoidanceType type) {
        this.pos = pos;
        this.type = type;
    }

    public void apply(Map<BlockPos, Double> map) {
        int radius = type.getRadius();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = x * x + y * y + z * z;
                    if (distance <= radius * radius) {
                        int rx = pos.getX() + x;
                        int ry = pos.getY() + y;
                        int rz = pos.getZ() + z;
                        BlockPos blockPos = new BlockPos(rx, ry, rz);
                        double coefficient = type.getCoefficient();
                        coefficient *= map.get(blockPos);
                        map.put(blockPos, coefficient);
                    }
                }
            }
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public AvoidanceType getType() {
        return type;
    }

    public static List<Avoidance> list(World world) {
        List<Avoidance> list = new ArrayList<>();
        List<Entity> entities = world.getEntities();
        for (Entity entity : entities) {
            AvoidanceType type = AvoidanceType.fromEntity(entity);
            if (type == null) continue;
            BlockPos pos = BlockPos.fromLocation(entity.getLocation());
            Avoidance avoidance = new Avoidance(pos, type);
            list.add(avoidance);
        }
        return list;
    }
}
