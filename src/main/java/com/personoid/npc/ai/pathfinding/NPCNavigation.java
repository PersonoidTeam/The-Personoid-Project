package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.newpathfinding.AStarPathfinder;
import com.personoid.npc.ai.newpathfinding.Node;
import com.personoid.npc.ai.newpathfinding.Path;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.LocationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class NPCNavigation extends NPCTickingComponent {
    private Path path;
    private final AStarPathfinder pathfinder = new AStarPathfinder(1000, true, 3);

    public NPCNavigation(NPC npc) {
        super(npc);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDone()) return;
        if (canUpdatePath()) {
            followPath();
        } else if (this.path != null && !this.path.isDone()) {
            Vec3 tempNPCPos = getTempNPCPos();
            Vec3 nextNPCPos = this.path.getNextNPCPos(this.npc);
            if (tempNPCPos.y > nextNPCPos.y && !this.npc.isOnGround() && Mth.floor(tempNPCPos.x) == Mth.floor(nextNPCPos.x) &&
                    Mth.floor(tempNPCPos.z) == Mth.floor(nextNPCPos.z)) {
                this.path.advance();
            }
        }
        if (isDone()) return;
        Vec3 nextNPCPos = this.path.getNextNPCPos(this.npc);
        BlockPos blockPos = new BlockPos(nextNPCPos);
        Vector velocity = new Vector(blockPos.getX() - this.npc.getX(), blockPos.getY() - this.npc.getY(), blockPos.getZ() - this.npc.getZ());
        this.npc.getMoveController().move(velocity);
        if (nextNPCPos.y > this.npc.getY() && this.npc.isOnGround()) {
            this.npc.getMoveController().jump();
        }
        if (path != null) {
            for (Node node : path.getNodes()) {
                this.npc.getBukkitEntity().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation().clone().add(0.5F, 0, 0.5F), 5,
                        new Particle.DustTransition(Color.YELLOW, Color.RED, 1));
            }
        }
    }

    public void moveTo(Location location) {
        // async leads to errors (concurrent modification exception) -> should be fast enough or synchronous running anyway
        Location groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        Location npcGroundLoc = LocationUtils.getBlockInDir(npc.getLocation(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        path = this.pathfinder.getPath(npcGroundLoc, groundLoc);
    }

    private void followPath() {
        Vec3 tempNPCPos = getTempNPCPos();
        float maxDistToWaypoint = (this.npc.getBbWidth() > 0.75F) ? (this.npc.getBbWidth() / 2) : (0.75F - this.npc.getBbWidth() / 2);
        BlockPos blockPos = this.path.getNextNodePos();
        double x = Math.abs(this.npc.getX() - blockPos.getX() + 0.5F);
        double y = Math.abs(this.npc.getY() - blockPos.getY());
        double z = Math.abs(this.npc.getZ() - blockPos.getZ() + 0.5F);
        boolean withinMaxDist = (x < maxDistToWaypoint && z < maxDistToWaypoint && y < 1D);
        if (true || (true && shouldTargetNextNodeInDirection(tempNPCPos))) { // TODO: 2nd true = this.npc.canCutCorner((this.path.getNextNode()).type)
            this.path.advance();
        }
        //doStuckDetection(tempNPCPos);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 tempNPCPos) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.size()) return false;
        Vec3 var1 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        if (!tempNPCPos.closerThan(var1, 2D)) return false;
        Vec3 var2 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
        Vec3 var3 = var2.subtract(var1);
        Vec3 var4 = tempNPCPos.subtract(var1);
        return var3.dot(var4) > 0D;
    }

    private Vec3 getTempNPCPos() {
        return new Vec3(this.npc.getX(), this.npc.getY(), this.npc.getZ());
    }

    private boolean canUpdatePath() {
        return (this.npc.isOnGround()); // TODO: or if in liquid
    }

    private boolean isDone() {
        return (this.path == null || this.path.isDone());
    }

    public void stop() {
        this.path = null;
    }

    private void trimPath() {
/*        if (this.path == null) return;
        for (int i = 0; i < this.path.size(); i++) {
            Node var1 = this.path.getNode(i);
            Node var2 = (i + 1 < this.path.size()) ? this.path.getNode(i + 1) : null;
            BlockState blockState = this.npc.level.getBlockState(new BlockPos(var1.getLocation().getX(), var1.getLocation().getY(), var1.getLocation().getZ()));
            if (blockState.is(BlockTags.CAULDRONS)) {
                this.path.replaceNode(i, var1.cloneAndMove(var1.getLocation().getBlockX(), var1.getLocation().getBlockY() + 1, var1.getLocation().getBlockZ()));
                if (var2 != null && var1.getLocation().getY() >= var2.getLocation().getY()) {
                    this.path.replaceNode(i + 1, var1.cloneAndMove(var2.getLocation().getBlockX(), var1.getLocation().getBlockY() + 1, var2.getLocation().getBlockZ()));
                }
            }
        }*/
    }
}
