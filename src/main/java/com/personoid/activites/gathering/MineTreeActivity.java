package com.personoid.activites.gathering;

import com.personoid.activites.interaction.BreakBlockActivity;
import com.personoid.activites.location.FindStructureActivity;
import com.personoid.activites.location.GoToLocationActivity;
import com.personoid.enums.LogType;
import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public class MineTreeActivity extends Activity {
    private final LogType logType;

    public MineTreeActivity() {
        super(ActivityType.GATHERING);
        logType = LogType.OAK; // TODO: search for nearest tree type? search for tree based on needs?
    }

    public MineTreeActivity(LogType logType) {
        super(ActivityType.GATHERING);
        this.logType = logType;
    }

    @Override
    public void onStart(StartType startType) {
        // this activity is made up of a sequence of activities + its own logic
        if (startType == StartType.START) {
            // runs the find structure activity (tree)
            tryFindTree();
        }
    }

    private void tryFindTree() {
        run(new FindStructureActivity(Structure.TREE, new FindStructureActivity.BlockInfo(logType.getBase())).onFinished((result) -> {
            // check if it found a tree
            if (result.getType() == Result.Type.SUCCESS) {
                // if it did, get the location of the tree
                Block block = result.getResult(Block.class);
                // TODO: check if it is correct log type
                // Ez
                Bukkit.broadcastMessage("Found tree at: " + block.getX() + ", " + block.getY() + ", " + block.getZ());
                mineTree(block);
            }
        }));
    }

    private void mineTree(Block block) {
        Location pathableLoc = LocationUtils.getPathableLocation(block.getLocation(), getActiveNPC().getLocation());
        run(new GoToLocationActivity(pathableLoc, 3).onFinished((result) -> {
            Bukkit.broadcastMessage("Mining tree at: " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            if (result.getType() == Result.Type.SUCCESS) {
                run(new BreakBlockActivity(block).onFinished((result1) -> {
                    if (result1.getType() == Result.Type.SUCCESS) {
                        Bukkit.broadcastMessage("Mined tree successfully!");
                        markAsFinished(new Result<>(Result.Type.SUCCESS, block));
                    } else {
                        Bukkit.broadcastMessage("Too far away from tree");
                        markAsFinished(new Result<>(Result.Type.FAILURE, block));
                    }
                }));
            } else tryFindTree();
        }));
    }

    @Override
    public void onUpdate() {
        if (isFinished()) {
            Bukkit.broadcastMessage("Finished mining tree");
            // TODO: shouldn't really be updating the activity after it's finished (even if it's only once)
        }
    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        Biome biome = getActiveNPC().getLocation().getBlock().getBiome();
        return hasTreesInBiome(biome); // TODO: look up which biomes have trees
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    private boolean hasTreesInBiome(Biome biome) {
        return switch (biome) {
            case OCEAN -> false;
            case PLAINS -> false;
            case DESERT -> false;
            case WINDSWEPT_HILLS -> true;
            case FOREST -> true;
            case TAIGA -> true;
            case SWAMP -> true;
            case RIVER -> false;
            case NETHER_WASTES -> true;
            case THE_END -> false;
            case FROZEN_OCEAN -> false;
            case FROZEN_RIVER -> false;
            case SNOWY_PLAINS -> true;
            case MUSHROOM_FIELDS -> false;
            case BEACH -> false;
            case JUNGLE -> true;
            case SPARSE_JUNGLE -> true;
            case DEEP_OCEAN -> false;
            case STONY_SHORE -> false;
            case SNOWY_BEACH -> false;
            case BIRCH_FOREST -> true;
            case DARK_FOREST -> true;
            case SNOWY_TAIGA -> true;
            case OLD_GROWTH_PINE_TAIGA -> true;
            case WINDSWEPT_FOREST -> true;
            case SAVANNA -> true;
            case SAVANNA_PLATEAU -> true;
            case BADLANDS -> false;
            case WOODED_BADLANDS -> true;
            case SMALL_END_ISLANDS -> false;
            case END_MIDLANDS -> true;
            case END_HIGHLANDS -> true;
            case END_BARRENS -> false;
            case WARM_OCEAN -> false;
            case LUKEWARM_OCEAN -> false;
            case COLD_OCEAN -> false;
            case DEEP_LUKEWARM_OCEAN -> false;
            case DEEP_COLD_OCEAN -> false;
            case DEEP_FROZEN_OCEAN -> false;
            case THE_VOID -> false;
            case SUNFLOWER_PLAINS -> false;
            case WINDSWEPT_GRAVELLY_HILLS -> true;
            case FLOWER_FOREST -> true;
            case ICE_SPIKES -> false;
            case OLD_GROWTH_BIRCH_FOREST -> true;
            case OLD_GROWTH_SPRUCE_TAIGA -> true;
            case WINDSWEPT_SAVANNA -> true;
            case ERODED_BADLANDS -> false;
            case BAMBOO_JUNGLE -> true;
            case SOUL_SAND_VALLEY -> false;
            case CRIMSON_FOREST -> true;
            case WARPED_FOREST -> false;
            case BASALT_DELTAS -> false;
            case DRIPSTONE_CAVES -> false;
            case LUSH_CAVES -> false;
            case MEADOW -> true;
            case GROVE -> false;
            case SNOWY_SLOPES -> false;
            case FROZEN_PEAKS -> false;
            case JAGGED_PEAKS -> false;
            case STONY_PEAKS -> false;
            case CUSTOM -> false;
        };
    }
}
