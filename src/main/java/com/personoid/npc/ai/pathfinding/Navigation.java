package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.pathfinding.requirements.WalkablePathRequirement;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.bukkit.Task;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.List;

public class Navigation extends NPCTickingComponent {
    private final Pathfinder pathfinder = new Pathfinder();
    private final double SPEED = 0.15D;
    private Path path;
    private double progress;
    private Vec3 currentPoint;
    private final Villager villager;
    private Location target;

    public Navigation(NPC npc) {
        super(npc);
        villager = new Villager(EntityType.VILLAGER, npc.getLevel());
    }
    
    @Override
    public void tick() {
        if (target != null) {
            updatePath();
            // let the GoToLocationActivity handle this
            npc.getLookController().face(target);
            if (path != null) {
                for (PathNode node : path.nodes()) {
                    npc.getBukkitEntity().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation().add(0.5F, 0, 0.5F), 5,
                            new Particle.DustTransition(Color.YELLOW, Color.RED, 1));
                }
                if (!updateLocation()) {
                    //this.path = null;
                }
            }
        }
    }

    public void setTarget(Location location) {
        target = location;
    }

    public Location getTarget() {
        return target;
    }

    public void updatePath() {
        // FIXME: goal location null
        if (target.distance(npc.getLocation()) > 1 && target != null) {
            new Task(() -> {
                Location endLoc = LocationUtils.getBlockInDir(target, BlockFace.DOWN).getLocation();
                Path path = pathfinder.findPath(npc.getLocation(), endLoc, List.of(new WalkablePathRequirement()), -1, 500);
                if (this.path == null && path != null) currentPoint = path.getNextNPCPos(npc);
                this.path = path;
            }).async().run();
        }
/*        Path tempPath = findPath(npc.getGoalSelector().getCurrentGoal().getTargetLocation(), 50);
        if (path == null && tempPath != null) currentPoint = tempPath.getNextNPCPos(npc);
        path = tempPath;
        if (path != null) {
            Bukkit.getPlayer(npc.spawner).sendMessage("Path: " + path.path);
            Node node = path.path.getNextNode();
            Location loc = new Location(npc.getLocation().getWorld(), node.asBlockPos().getX(), node.asBlockPos().getY(), node.asBlockPos().getZ());
            npc.getBukkitEntity().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc.add(0.5F, 0, 0.5F), 5,
                    new Particle.DustTransition(Color.YELLOW, Color.RED, 1));
            if (!updateLocation()) {
                this.path = null;
            }
        }*/
    }

    public Path getPath() {
        return path;
    }

    public boolean updateLocation() {
        int current = Mth.floor(progress);
        double d = progress - current;
        double d1 = 1 - d;
        if (d + SPEED < 1) {
            double dx = (currentPoint.x - npc.getX()) * SPEED;
            double dz = (currentPoint.z - npc.getZ()) * SPEED;

            npc.getMoveController().move(new Vector(dx, 0, dz));
            npc.checkMovementStatistics(dx, 0, dz);
            progress += SPEED;
        } else {
            //First complete old point.
            double bx = (currentPoint.x - npc.getX()) * d1;
            double bz = (currentPoint.z - npc.getZ()) * d1;

            //Check if new point exists
            path.advance();
            if (!path.notStarted()) {
                //Append new movement
                currentPoint = path.getNextNPCPos(npc);
                double d2 = SPEED - d1;

                double dx = bx + ((currentPoint.x - npc.getX()) * d2);
                double dy = currentPoint.y - npc.getY(); //Jump if needed to reach next block.
                double dz = bz + ((currentPoint.z - npc.getZ()) * d2);

                npc.getMoveController().move(new Vector(dx, dy, dz));
                npc.checkMovementStatistics(dx, dy, dz);
                progress += SPEED;
            } else {
                //Complete final movement
                npc.getMoveController().move(new Vector(bx, 0, bz));
                npc.checkMovementStatistics(bx, 0, bz);
                return false;
            }
        }
        return true;
    }

/*    public Path findPath(Location to, double range) {
        try {
            double y = LocationUtils.getBlockInDir(npc.getLocation(), BlockFace.DOWN).getRelative(BlockFace.UP).getY();
            villager.setPos(npc.getX(), y, npc.getZ());
            BlockPos fromPos = new BlockPos(npc.getX(), npc.getY(), npc.getZ());
            BlockPos toPos = new BlockPos(to.getX(), to.getY(), to.getZ());
            int k = (int) (range + 8);
            PathNavigationRegion region = new PathNavigationRegion(npc.getLevel(), fromPos.offset(-k, -k, -k), fromPos.offset(k, k, k));
            return new Path(npc.getPathFinder().findPath(region, villager, ImmutableSet.of(toPos), (float) range, 1, 15));
        } catch (Exception e) {
            return null;
        }
    }*/
}
