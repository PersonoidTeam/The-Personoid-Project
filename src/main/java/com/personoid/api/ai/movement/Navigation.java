package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.Node;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.pathfinding.Pathfinder;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class Navigation {
    private final NPC npc;
    private final Pathfinder pathfinder = new Pathfinder();
    private final Options options = new Options();
    private Path path;

    private int groundTicks;

    public Navigation(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (npc.isOnGround() && groundTicks < Integer.MAX_VALUE) groundTicks++;
        if (isDone()) return;
        if (canUpdatePath()) {
            followPath();
        } else if (path != null && !path.isDone()) {
            Vector tempNPCPos = getTempNPCPos();
            Vector nextNPCPos = path.getNextNPCPos(npc);
            if (tempNPCPos.getY() > nextNPCPos.getY() && !npc.isOnGround() && Math.floor(tempNPCPos.getX()) == Math.floor(nextNPCPos.getX()) &&
                    Math.floor(tempNPCPos.getZ()) == Math.floor(nextNPCPos.getZ())) {
                Profiler.NAVIGATION.push("advancing to next node 1");
                path.advance();
            }
        }
        if (isDone()) return;

        // movement
        Vector nextNPCPos = path.getNextNPCPos(npc);
        Location nextLoc = new Location(npc.getLocation().getWorld(), nextNPCPos.getX(), nextNPCPos.getY(), nextNPCPos.getZ());
        Vector velocity = nextLoc.toVector().subtract(npc.getLocation().toVector()).normalize();
        Vector lerpedVelocity = MathUtils.lerpVector(npc.getMoveController().getVelocity(), velocity, options.getMovementSmoothing());
        lerpedVelocity.setY(velocity.getY());
        npc.getMoveController().move(lerpedVelocity, options.movementType);
        if (shouldJump()) npc.getMoveController().jump();

        // movement specifics
        if (nextNPCPos.getY() >= npc.getLocation().getY() + options.getMaxStepHeight()) {
            Block blockDown = nextLoc.getBlock().getRelative(BlockFace.DOWN);
            if (npc.isOnGround()) {
                if (blockDown.getType().name().contains("STAIRS")) {
                    npc.getMoveController().step(((nextNPCPos.getY() - npc.getLocation().getY()) / 2) * 1.2F); // move up to half stair height
                    Profiler.NAVIGATION.push("stepping on stairs");
                } else if (BlockTags.CLIMBABLE.is(blockDown.getType())) {
                    npc.getMoveController().step(0.2F);
                    Profiler.NAVIGATION.push("climbing");
                }/* else if (path.getNPCPosAtNode(npc, path.getNextNodeIndex() + 1).getY() >
                        npc.getLocation().getY() + options.getMaxStepHeight()) {
                    if (groundTicks >= 4) {
                        npc.getMoveController().jump();
                        Profiler.NAVIGATION.push("jumped");
                    }
                }*/
            }
        } else {
            double diff = nextNPCPos.getY() - npc.getLocation().getY();
            if (diff > 0 && diff < options.getMaxStepHeight()) {
                npc.getMoveController().step((nextNPCPos.getY() - npc.getLocation().getY()) * 1.2F);
            }
        }
        if (path != null) {
            for (Node node : path.getNodes()) {
                npc.getLocation().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation().clone().add(0.5F, 0, 0.5F), 5,
                        new Particle.DustTransition(Color.RED, Color.ORANGE, 1));
            }
        }
        //trimPath();
    }

    private boolean shouldJump() {
        int blockadeDist = Integer.MAX_VALUE;
        for (int i = 0; i <= 3; i++) {
            Vector lookAheadPos = path.getNPCPosAtNode(npc, path.getNextNodeIndex() + i);
            if (lookAheadPos.getY() < npc.getLocation().getY() - 2) {
                return false; // jumping here would result in the npc taking fall damage
            }
            if (lookAheadPos.getY() > npc.getLocation().getY() + options.maxStepHeight) {
                blockadeDist = i;
                break;
            }
        }
        if (options.movementType == MovementType.SPRINT_JUMPING) {
            if (blockadeDist == 3) return false;
            else if (blockadeDist > 3) return true;
        }
        if (npc.isOnGround() && groundTicks >= 4) {
            if (options.movementType.name().contains("SPRINT")) {
                return blockadeDist <= 2;
            } else if (options.movementType == MovementType.WALKING) {
                return blockadeDist <= 1;
            }
            return true;
        }
        return false;
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public Block moveTo(Location location, MovementType movementType) {
        options.movementType = movementType;
        Location groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        Location npcGroundLoc = LocationUtils.getBlockInDir(npc.getLocation(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        // FIXME: async leads to errors (concurrent modification exception) -> should be fast enough or synchronous running anyway
        path = pathfinder.getPath(npcGroundLoc, groundLoc);
        if (path != null) Profiler.NAVIGATION.push("found path, length: " + path.size());
        return groundLoc.getBlock();
    }

    private void followPath() {
        Vector tempNPCPos = getTempNPCPos();
        double maxDistToWaypoint = 0.45;
        Vector blockPos = path.getNextNodePos();
        Block block = new Location(npc.getLocation().getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ()).getBlock();
        double x = Math.abs(npc.getLocation().getX() - blockPos.getX() + 0.5);
        double y = Math.abs(npc.getLocation().getY() - blockPos.getY());
        double z = Math.abs(npc.getLocation().getZ() - blockPos.getZ() + 0.5);
        boolean withinMaxDist = (x < maxDistToWaypoint && z < maxDistToWaypoint && y < 1D); //y < 1D
        if (withinMaxDist || (canCutCorner(block.getType()) && shouldTargetNextNode(tempNPCPos))) {
            Profiler.NAVIGATION.push("advancing to next node 2");
            this.path.advance();
        }
        //doStuckDetection(tempNPCPos);
    }

    private boolean canCutCorner(Material material) {
        boolean canCutCorner = !material.name().contains("FIRE") && !material.name().contains("CACTUS") && !material.name().contains("DOOR") &&
                !material.name().contains("LAVA") && !material.name().contains("COBWEB");
        Profiler.NAVIGATION.push("canCutCorner: " + canCutCorner);
        return canCutCorner;
    }

    private boolean shouldTargetNextNode(Vector tempNPCPos) {
        if (path.getNextNodeIndex() + 1 >= path.size()) return false;
        Vector center = LocationUtils.atBottomCenterOf(path.getNextNodePos());
        if (!LocationUtils.closerThan(tempNPCPos, center, 2)) return false;
        Vector nextCenter = LocationUtils.atBottomCenterOf(path.getNodePos(path.getNextNodeIndex() + 1));
        Vector nextNodeDiff = nextCenter.subtract(center);
        Vector tempPosDiff = tempNPCPos.subtract(center);
        Profiler.NAVIGATION.push("shouldTargetNextNode: " + (nextNodeDiff.clone().dot(tempPosDiff) > 0));
        return nextNodeDiff.dot(tempPosDiff) > 0;
    }

    private Vector getTempNPCPos() {
        return npc.getLocation().toVector();
    }

    private boolean canUpdatePath() {
        Profiler.NAVIGATION.push("can update path: " + npc.isOnGround());
        return (npc.isOnGround()); // TODO: or if in liquid
    }

    private boolean isDone() {
        return (path == null || path.isDone());
    }

    public void stop() {
        path = null;
    }

/*    private void trimPath() {
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
    }*/

    public Options getOptions() {
        return options;
    }

    public static class Options {
        private float maxStepHeight = 0.3F;
        private float movementSmoothing = 0.2F;
        private MovementType movementType;

        public float getMaxStepHeight() {
            return maxStepHeight;
        }

        public void setMaxStepHeight(float maxStepHeight) {
            this.maxStepHeight = maxStepHeight;
        }

        public float getMovementSmoothing() {
            return movementSmoothing;
        }

        public void setMovementSmoothing(float movementSmoothing) {
            this.movementSmoothing = movementSmoothing;
        }

        public MovementType getMovementType() {
            return movementType;
        }

        public void setMovementType(MovementType movementType) {
            this.movementType = movementType;
        }
    }
}
