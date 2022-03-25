package com.personoid.activites;

import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindStructureActivity extends Activity {

    // This entire mining system is a literal nightmare.

    private final Structure structure;
    private final BlockInfo info;
    private final List<Location> attempted = new ArrayList<>();

    public FindStructureActivity(Structure structure, BlockInfo info) {
        super(ActivityType.SEARCHING);
        this.structure = structure;
        this.info = info;
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

        // Only future proof solution I could think of.
        Material treeType = info.get(0);

        switch (structure) {
            case TREE -> {
                Location tree = checkForTree(treeType);
                if (tree != null) {
                    markAsFinished(new Result<>(Result.Type.SUCCESS, tree.getBlock()));
                } else {
                    Location loc = getActiveNPC().getLocation().add(MathUtils.random(-50, 50), 0, MathUtils.random(-50, 50));
                    loc = new Location(loc.getWorld(), loc.getX(), LocationUtils.getAirInDir(loc.subtract(0, 1, 0), BlockFace.UP).getLocation().getY(), loc.getZ());
                    Bukkit.broadcastMessage("Trying new location: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                    attempted.add(loc);
                    run(new GoToLocationActivity(loc, 4).onFinished((result) -> checkLocation()));
                }
            }
            default -> throw new NullPointerException("No method set for structure: " + structure.name());
        }


    }

    private Location checkForTree(Material material) {
        // TODO: better tree detection
        // check every block in 20 block radius
        Bukkit.broadcastMessage("Finding logs...");
        return checkFrom(-15, 15, true, material);
    }

    private Location checkFrom(int min, int max, boolean flipExclusions, Material... exclude) {
        List<Material> exclusions = Arrays.asList(exclude);

        Location closestLog = null;

        for (int x = min; x <= max; x++) {
            for (int y = min; y <= max; y++) { // can't see underground?
                for (int z = min; z <= max; z++) {
                    Location checkLoc = getActiveNPC().getLocation().clone().add(x, y, z);

                    // Exclusion check.
                    Material material = checkLoc.getBlock().getType();

                    if (flipExclusions){
                        if (!exclusions.contains(material)){
                            continue;
                        }
                    }
                    else {
                        if (exclusions.contains(material)){
                            continue;
                        }
                    }

                    Bukkit.broadcastMessage("We want: "+material);

                    if (material.isSolid()) {
                        if (material.toString().toLowerCase().contains("log")) {
                            // found a log
                            Bukkit.broadcastMessage("Found a log at: " + checkLoc.getBlockX() + ", " + checkLoc.getBlockY() + ", " + checkLoc.getBlockZ());
                            Bukkit.broadcastMessage("Log type: " + checkLoc.getBlock().getType());
                            if (closestLog == null){
                                closestLog = checkLoc;
                            }
                            if (checkLoc.distance(getActiveNPC().getLocation()) < closestLog.distance(getActiveNPC().getLocation())){
                                closestLog = checkLoc;
                            }
                        }
                    }
                }
            }
        }
        return closestLog;
    }

    // Might not be helpful if used improperly, theoretically shouldnt be a problem though.
    // Dont know how performant raytrace result is.
    private boolean isOccluded(Location check, Location center, int maxDistance){
        RayTraceResult result = check.getWorld().rayTrace(check, check.toVector().subtract(center.toVector()), maxDistance, FluidCollisionMode.NEVER,
                true, maxDistance, null);

        if (result == null){
            return false;
        }
        if (result.getHitBlock() == null){
            return false;
        }
        Bukkit.broadcastMessage(check.getBlock().getType()+" Is occluded!");
        return true;
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

    public static class BlockInfo {

        Material[] info;

        public BlockInfo(Material... blocks){
            info = blocks;
        }

        public Material get(int index){
            return info[index];
        }
    }
}