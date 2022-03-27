package com.personoid.activites;

import com.personoid.enums.Structure;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.npc.ai.activity.Result;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

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
                    loc = new Location(loc.getWorld(), loc.getX(), LocationUtils.getAirInDir(
                            loc.subtract(0, 1, 0), BlockFace.UP).getLocation().getY(), loc.getZ());
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
        return checkFrom(-20, 20, true, material);
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

                    Location pathableLocation = LocationUtils.getPathableLocation(checkLoc, getActiveNPC().getLocation());

                    if (pathableLocation == null){
                        continue;
                    }

                    if (!LocationUtils.canReach(checkLoc, pathableLocation)) {
                        continue;
                    }

                    if (isOccluded(checkLoc, pathableLocation, 6)){
                        continue;
                    }

                    //Bukkit.broadcastMessage("We want: "+material);

                    if (material.isSolid()) {
                        if (material.toString().toLowerCase().contains("log")) {
                            // found a log
                            //Bukkit.broadcastMessage("Found a log at: " + checkLoc.getBlockX() + ", " + checkLoc.getBlockY() + ", " + checkLoc.getBlockZ());
                            //Bukkit.broadcastMessage("Log type: " + checkLoc.getBlock().getType());
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
    private boolean isOccluded(Location from, Location to, int maxDistance){
/*        RayTraceResult result = check.getWorld().rayTrace(check.add(0.5, 0.5, 0.5), center.toVector().subtract(check.toVector()), maxDistance, FluidCollisionMode.NEVER,
                true, maxDistance, null);*/

        Block hit = LocationUtils.rayTraceBlocks(from, to, maxDistance, false);
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (hit != null) {
                player.sendBlockChange(hit.getLocation(), Bukkit.createBlockData(Material.GOLD_BLOCK));
            }
        });

        if (hit == null){
            return false;
        }
/*        if (result.getHitBlock() == null){
            return false;
        }*/
        else {

            if (hit.getType().equals(from.getBlock().getType())){
                return false;
            }
            else {
                getActiveNPC().getLocation().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, hit.getLocation()
                        , 10, new Particle.DustTransition(Color.BLUE, Color.AQUA, 2));
            }
        }
        //Bukkit.broadcastMessage(check.getBlock().getType()+" Is occluded!");
        return true;
    }

/*    public boolean traceLocation(Location g, Location l, Player p) {
        double x = g.getX()-l.getX(), y = g.getY()-l.getY(), z = g.getZ()-l.getZ();
        Vector direction = new Vector(x, y, z).normalize();
        p.sendMessage(""+g.distanceSquared(l));
        for (int i = 0; i < 15; i++) {
            l.add(direction);
            if (l.getBlock().getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }*/

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
