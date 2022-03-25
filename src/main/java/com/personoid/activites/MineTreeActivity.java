package com.personoid.activites;

import com.personoid.enums.LogType;
import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class MineTreeActivity extends Activity {
    private final LogType logType;

    public MineTreeActivity() {
        super(ActivityType.GATHERING);
        logType = LogType.OAK; // search for nearest tree type?
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
        Location loc = LocationUtils.getNear(getActiveNPC().getLocation(), block.getLocation(), 100);
        run(new GoToLocationActivity(loc, 5).onFinished((result) -> {
            Bukkit.broadcastMessage("Mining tree at: " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            if (result.getType() == Result.Type.SUCCESS) {
                Bukkit.broadcastMessage(getActiveNPC().getMoveController().getVelocity().toString());
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

    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }
}
