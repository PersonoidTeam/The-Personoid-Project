package com.personoid.api.pathfindingold;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private static final Map<World, CacheManager> cacheManagers = new HashMap<>();

    private final World world;
    private final Map<BlockPos, BlockType> blockTypeMap = new HashMap<>();

    public CacheManager(World world) {
        this.world = world;
    }

    public static CacheManager get(World world) {
        return cacheManagers.computeIfAbsent(world, CacheManager::new);
    }

    public World getWorld() {
        return world;
    }

    public void updateCache(BlockPos pos) {
        blockTypeMap.clear();
    }

    public Block getBlock(BlockPos pos) {
        return world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockType getBlockType(Node node) {
        return getBlockType(new BlockPos(node.getX(), node.getY(), node.getZ()));
    }

    public BlockType getBlockType(BlockPos pos) {
        Material type = getBlock(pos).getType();

        if (blockTypeMap.containsKey(pos)) return blockTypeMap.get(pos);
        BlockType blockType;

        // TODO: BlockType class should determine this
        if (type == Material.WATER) blockType = BlockType.WATER;
        else if (type == Material.LAVA) blockType = BlockType.DANGER;
        else if (type.isSolid()) blockType = BlockType.SOLID;
        else blockType = BlockType.AIR;

        blockTypeMap.put(pos, blockType);
        return blockType;
    }
}
