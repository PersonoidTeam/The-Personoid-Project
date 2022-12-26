package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.Node;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.pathfinding.Pathfinder;
import com.personoid.api.utils.LocationUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class Navigation {
    private final NPC npc;
    private final Pathfinder pathfinder = new Pathfinder();
    private final Options options = new Options();
    private Path path;
    private Location goal;

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
                path.advance();
            }
        }
        if (isDone()) return;

/*        // movement
        Vector nextNPCPos = npc.isInWater() && options.straightLineInWater ? goal.toVector() : path.getNextNPCPos(npc);
        if (options.straightLine) {
            Location groundLoc = LocationUtils.getBlockInDir(goal, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
            nextNPCPos = groundLoc.toVector();
        }*/

        if (shouldJump()) npc.getMoveController().jump();

/*        if (BlockTags.CLIMBABLE.is(npc.getWorld().getBlockAt(nextNPCPos.toLocation(npc.getWorld())).getType())) {
            npc.getMoveController().step(0.15F);
            Profiler.NAVIGATION.push("climbing");
        }*/

        if (path != null) {
            for (Node node : path.getNodes()) {
                npc.getLocation().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation().clone().add(0.5F, 0, 0.5F), 5,
                        new Particle.DustTransition(Color.RED, Color.ORANGE, 1));
            }
        }
    }

    private boolean shouldJump() {
        //if (npc.getMoveController().isClimbing()) return false;
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
        goal = location.clone();
        if ((!options.straightLineInWater || !npc.isInWater()) && !options.straightLine) {
            path = pathfinder.getPath(npcGroundLoc, groundLoc);
            path.clean();
        } else path = null;
        Vector nextNPCPos = npc.isInWater() && options.straightLineInWater ? goal.toVector() : path.getNPCPosAtNode(npc, path.getNextNodeIndex() + 1);
        npc.getMoveController().moveTo(nextNPCPos.getX(), nextNPCPos.getZ(), options.movementType);
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
            this.path.advance();
            // we target the next-next node so that the move controller doesn't jitter the yaw as it gets closer to the next node
            Vector nextNPCPos = npc.isInWater() && options.straightLineInWater ? goal.toVector() : path.getNPCPosAtNode(npc, path.getNextNodeIndex() + 1);
            npc.getMoveController().moveTo(nextNPCPos.getX(), nextNPCPos.getZ(), options.movementType);
        }
        //doStuckDetection(tempNPCPos);
    }

    private boolean canCutCorner(Material material) {
        String name = material.name();
        return !name.contains("FIRE") &&
                !name.contains("CACTUS") &&
                !name.contains("DOOR") &&
                !name.contains("LAVA") &&
                !name.contains("COBWEB");
    }

    private boolean shouldTargetNextNode(Vector tempNPCPos) {
        if (path.getNextNodeIndex() + 1 >= path.size()) return false;
        Vector center = LocationUtils.atBottomCenterOf(path.getNextNodePos());
        if (!LocationUtils.closerThan(tempNPCPos, center, 2)) return false;
        Vector nextCenter = LocationUtils.atBottomCenterOf(path.getNodePos(path.getNextNodeIndex() + 1));
        Vector nextNodeDiff = nextCenter.subtract(center);
        Vector tempPosDiff = tempNPCPos.subtract(center);
        return nextNodeDiff.dot(tempPosDiff) > 0;
    }

    private Vector getTempNPCPos() {
        return npc.getLocation().toVector();
    }

    private boolean canUpdatePath() {
        return npc.isOnGround();
    }

    private boolean isDone() {
        return (path == null || path.isDone());
    }

    public void stop() {
        path = null;
    }

    public Options getOptions() {
        return options;
    }

    public static class Options {
        private float maxStepHeight = 0.3F;
        private float movementSmoothing = 0.2F;
        private MovementType movementType;
        private boolean straightLineInWater = true;
        private boolean straightLine;

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

        public boolean isStraightLineInWater() {
            return straightLineInWater;
        }

        public void setStraightLineInWater(boolean straightLineInWater) {
            this.straightLineInWater = straightLineInWater;
        }

        public boolean isStraightLine() {
            return straightLine;
        }

        public void setStraightLine(boolean straightLine) {
            this.straightLine = straightLine;
        }
    }
}
