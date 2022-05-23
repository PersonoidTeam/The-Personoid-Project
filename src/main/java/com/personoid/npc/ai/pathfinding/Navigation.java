package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.LocationUtils;
import com.personoid.utils.MathUtils;
import com.personoid.utils.values.BlockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class Navigation extends NPCTickingComponent {
    private final Pathfinder pathfinder = new Pathfinder(500, new Pathfinder.Options(2, true, true));
    private Options options;
    private MovementType movementType;
    private Path path;

    public Navigation(NPC npc, Options options) {
        super(npc);
        this.options = options;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDone()) return;
        if (canUpdatePath()) {
            followPath();
        } else if (path != null && !path.isDone()) {
            Vec3 tempNPCPos = getTempNPCPos();
            Vec3 nextNPCPos = path.getNextNPCPos(npc);
            if (tempNPCPos.y > nextNPCPos.y && !npc.isOnGround() && Mth.floor(tempNPCPos.x) == Mth.floor(nextNPCPos.x) &&
                    Mth.floor(tempNPCPos.z) == Mth.floor(nextNPCPos.z)) {
                path.advance();
            }
        }
        if (isDone()) return;

        // movement
        Vec3 nextNPCPos = path.getNextNPCPos(npc);
        Location nextLoc = new Location(npc.getLocation().getWorld(), nextNPCPos.x, nextNPCPos.y, nextNPCPos.z);
        Vector velocity = new Vector(nextLoc.getX() - npc.getX(), nextLoc.getY() - npc.getY(), nextLoc.getZ() - npc.getZ());
        Vector lerpedVelocity = MathUtils.lerpVector(npc.getMoveController().getVelocity(), velocity, options.smoothing);
        lerpedVelocity.setY(velocity.getY());
        npc.getMoveController().move(lerpedVelocity, movementType);

        // check if next next npc pos is one block up from current
        if (movementType == MovementType.SPRINT_JUMPING) {
            int count = 0;
            for (int i = 0; i < 3; i++) {
                Vec3 nextIndexNPCPos = path.getNPCPosAtNode(npc, path.getNextNodeIndex() + i);
                if (nextIndexNPCPos.y > npc.getY()) {
                    count++;
                }
            }
            if (count == 0) {
                npc.getMoveController().jump();
            }
        }

        // movement specifics
        if (nextNPCPos.y >= npc.getY() + options.getMaxStepHeight()) {
            Block blockDown = nextLoc.getBlock().getRelative(BlockFace.DOWN);
            if (npc.isOnGround()) {
                if (blockDown.getType().name().contains("STAIRS")) {
                    npc.getMoveController().step(((nextNPCPos.y - npc.getY()) / 2) * 1.2F); // move up to half stair height
                } else if (BlockTypes.isClimbable(blockDown.getType())) {
                    npc.getMoveController().step(0.2F);
                } else if (path.getNPCPosAtNode(npc, path.getNextNodeIndex() + 1).y > npc.getY() + options.getMaxStepHeight()) {
                    if (npc.getGroundTicks() >= 4) npc.getMoveController().jump();
                }
            }
        } else {
            double diff = nextNPCPos.y - npc.getY();
            if (diff > 0 && diff < options.getMaxStepHeight()) {
                npc.getMoveController().step((nextNPCPos.y - npc.getY()) * 1.2F);
            }
        }
        if (path != null) {
            for (Node node : path.getNodes()) {
                npc.getBukkitEntity().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation().clone().add(0.5F, 0, 0.5F), 5,
                        new Particle.DustTransition(Color.YELLOW, Color.RED, 1));
            }
        }
        //trimPath();
    }

    public void moveTo(Location location, MovementType movementType) {
        this.movementType = movementType;
        Location groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        Location npcGroundLoc = LocationUtils.getBlockInDir(npc.getLocation(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        // async leads to errors (concurrent modification exception) -> should be fast enough or synchronous running anyway
        path = pathfinder.getPath(npcGroundLoc, groundLoc);
    }

    private void followPath() {
        Vec3 tempNPCPos = getTempNPCPos();
        float maxDistToWaypoint = (npc.getBbWidth() > 0.75F) ? (npc.getBbWidth() / 2F) : (0.75F - npc.getBbWidth() / 2F);
        BlockPos blockPos = path.getNextNodePos();
        Block block = new Location(npc.getLocation().getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ()).getBlock();
        double x = Math.abs(npc.getX() - blockPos.getX() + 0.5);
        double y = Math.abs(npc.getY() - blockPos.getY());
        double z = Math.abs(npc.getZ() - blockPos.getZ() + 0.5);
        boolean withinMaxDist = (x < maxDistToWaypoint && z < maxDistToWaypoint && y < maxDistToWaypoint); //y < 1D
        if (withinMaxDist || (canCutCorner(block.getType()) && shouldTargetNextNode(tempNPCPos))) {
            this.path.advance();
        }
        //doStuckDetection(tempNPCPos);
    }

    private boolean canCutCorner(Material material) {
        return !material.name().contains("FIRE") && !material.name().contains("CACTUS") && !material.name().contains("DOOR") &&
                !material.name().contains("LAVA") && !material.name().contains("COBWEB");
    }

    private boolean shouldTargetNextNode(Vec3 tempNPCPos) {
        if (path.getNextNodeIndex() + 1 >= path.size()) return false;
        Vec3 center = Vec3.atBottomCenterOf(path.getNextNodePos());
        if (!tempNPCPos.closerThan(center, 2)) return false;
        Vec3 nextCenter = Vec3.atBottomCenterOf(path.getNodePos(path.getNextNodeIndex() + 1));
        Vec3 nextNodeDiff = nextCenter.subtract(center);
        Vec3 tempPosDiff = tempNPCPos.subtract(center);
        return nextNodeDiff.dot(tempPosDiff) > 0;
    }

    private Vec3 getTempNPCPos() {
        return new Vec3(npc.getX(), npc.getY(), npc.getZ());
    }

    private boolean canUpdatePath() {
        return (npc.isOnGround()); // TODO: or if in liquid
    }

    private boolean isDone() {
        return (path == null || path.isDone());
    }

    public void stop() {
        path = null;
    }

    private void trimPath() {
        if (path == null) return;
        for (int i = 0; i < path.size(); i++) {
            Node node = path.getNode(i);
            Node nextNode = (i + 1 < path.size()) ? path.getNode(i + 1) : null;
            BlockState blockState = npc.level.getBlockState(new BlockPos(node.getX(), node.getY(), node.getZ()));
            if (blockState.is(BlockTags.CAULDRONS)) {
                path.replaceNode(i, node.cloneAndMove(node.getX(), node.getY() + 1, node.getZ()));
                if (nextNode != null && node.getLocation().getY() >= nextNode.getLocation().getY()) {
                    path.replaceNode(i + 1, node.cloneAndMove(nextNode.getX(), node.getY() + 1, nextNode.getZ()));
                }
            }
        }
    }

    public static class Options {
        private float maxStepHeight = 0.3F;
        private float smoothing = 0.075F;

        public float getMaxStepHeight() {
            return maxStepHeight;
        }

        public void setMaxStepHeight(float maxStepHeight) {
            this.maxStepHeight = maxStepHeight;
        }

        public float getSmoothing() {
            return smoothing;
        }

        public void setSmoothing(float smoothing) {
            this.smoothing = smoothing;
        }
    }
}
