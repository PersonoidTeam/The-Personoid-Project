package com.personoid.activites;

import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class FindStructureActivity extends Activity {
    private final Structure structure;
    private final List<Location> attempted = new ArrayList<>();

    public FindStructureActivity(Structure structure) {
        super(ActivityType.SEARCHING);
        this.structure = structure;
    }

    @Override
    public void onStart(StartType startType) {
        attempted.clear(); // acts like a 'try again after a break'
        if (startType == StartType.START) {
            checkLocation();
        }
    }

    private void checkLocation() {
        if (attempted.size() >= 8) { // tweak this value
            markAsFinished(new Result<>(Result.Type.FAILURE, attempted));
        }

        Location tree = checkForTree();
        if (tree != null) {
            markAsFinished(new Result<>(Result.Type.SUCCESS, tree.getBlock()));
        } else {
            Location loc = getActiveNPC().getLocation().add(MathUtils.random(-50, 50), 0, MathUtils.random(-50, 50));
            loc = new Location(loc.getWorld(), loc.getX(), LocationUtils.getAirInDir(loc.subtract(0, 1, 0), BlockFace.UP).getLocation().getY(), loc.getZ());
            Bukkit.broadcastMessage("Trying new location: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            attempted.add(loc);

            run(new GoToLocationActivity(loc).onFinished((result) -> {
                if (result.getType() == Result.Type.SUCCESS) {
                    checkLocation();
                } else checkLocation();
            }));
        }
    }

    private Location checkForTree() {
        // TODO: better tree detection
        // check every block in 20 block radius
        Bukkit.broadcastMessage("Finding logs...");
        Location loc1 = checkFrom(0, 15);
        if (loc1 != null) return loc1;
        else return checkFrom(-15, 0);
    }

    private Location checkFrom(int min, int max) {
        for (int x = min; x <= max; x++) {
            for (int y = -1; y <= 4; y++) { // can't see underground?
                for (int z = min; z <= max; z++) {
                    Location checkLoc = getActiveNPC().getLocation().clone().add(x, y, z);
                    if (checkLoc.getBlock().getType().isSolid()) {
                        if (checkLoc.getBlock().getType().toString().toLowerCase().contains("log")) {
                            // found a log
                            Bukkit.broadcastMessage("Found a log at: " + checkLoc.getBlockX() + ", " + checkLoc.getBlockY() + ", " + checkLoc.getBlockZ());
                            Bukkit.broadcastMessage("Log type: " + checkLoc.getBlock().getType());
                            return checkLoc;
                        }
                    }
                }
            }
        }
        return null;
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
