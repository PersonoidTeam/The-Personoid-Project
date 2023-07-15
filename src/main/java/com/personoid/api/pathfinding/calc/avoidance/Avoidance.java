package com.personoid.api.pathfinding.calc.avoidance;

import com.personoid.api.pathfinding.calc.utils.BlockPos;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Avoidance {
    private final BlockPos pos;
    private final AvoidanceType type;

    public Avoidance(BlockPos pos, AvoidanceType type) {
        this.pos = pos;
        this.type = type;
    }

    public void apply(Long2DoubleOpenHashMap map) {
        int radius = type.getRadius();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distance = x * x + y * y + z * z;
                    if (distance <= radius * radius) {
                        int rx = pos.getX() + x;
                        int ry = pos.getY() + y;
                        int rz = pos.getZ() + z;
                        long hash = new BlockPos(rx, ry, rz).asLong();
                        double coefficient = type.getCoefficient();
                        coefficient *= map.get(hash);
                        map.put(hash, coefficient);
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
