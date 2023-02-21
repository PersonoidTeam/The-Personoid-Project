package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.pathfinding.PathFinder;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.types.BlockTags;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class Navigation {
    private final NPC npc;
    private final PathFinder pathfinder = new PathFinder();
    private final Options options = new Options();
    private Path path;
    private Location goal;

    private int groundTicks;

    public Navigation(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (npc.isOnGround() && groundTicks < Integer.MAX_VALUE) groundTicks++;
        if (getOptions().straightLine || (getOptions().straightLineInWater && npc.isInWater()) && goal != null) {
            npc.getMoveController().moveTo(goal.getX(), goal.getZ());
        } else if (canUpdatePath()) {
            followPath();
        }
            /* else if (path != null && !path.isDone()) {
            Vector tempNPCPos = getTempNPCPos();
            Vector nextNPCPos = path.getNextNPCPos(npc);
            if (tempNPCPos.getY() > nextNPCPos.getY() && !npc.isOnGround() && Math.floor(tempNPCPos.getX()) == Math.floor(nextNPCPos.getX()) &&
                    Math.floor(tempNPCPos.getZ()) == Math.floor(nextNPCPos.getZ())) {
                path.advance();
            }
        }*/

        if (shouldJump()) {
            npc.getMoveController().jump();
        }

        if (BlockTags.CLIMBABLE.is(npc.getLocation())) {
            npc.getMoveController().setClimbing(true);
            npc.getMoveController().applyUpwardForce(0.1F);
            Profiler.NAVIGATION.push("climbing");
        } else {
            npc.getMoveController().setClimbing(false);
        }

        if (path != null && options.showPath) {
            for (int i = 0; i < path.size(); i++) {
                Node node = path.getNode(i);
                Particle.DustTransition dustTransition = i == path.getNextNodeIndex() ? new Particle.DustTransition(Color.BLUE, Color.PURPLE, 0.8F) :
                        new Particle.DustTransition(Color.RED, Color.ORANGE, 0.8F);
                npc.getLocation().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                        node.getPosition().toLocation(npc.getWorld()).clone().add(0.5F, 0, 0.5F), 3, dustTransition);
            }
        }
    }

    private boolean shouldJump() {
        if (npc.getMoveController().isClimbing() || path == null || npc.isMovingInWater()) return false;
        int blockadeDist = Integer.MAX_VALUE;
        for (int i = 0; i <= 3; i++) {
            Vector lookAheadPos = path.getNPCPosAtNode(npc, path.getNextNodeIndex() + i);
            if (lookAheadPos.getY() < npc.getLocation().getY() - 2) {
                return false; // jumping here would result in the npc taking fall damage
            }
            if (lookAheadPos.toLocation(npc.getWorld()).getBlock().getType().name().contains("STAIRS")) {
                return false; // don't want to jump on stairs
            }
            if (lookAheadPos.getY() > npc.getLocation().getY() + options.maxStepHeight) {
                blockadeDist = i;
                break;
            }
        }
        if (npc.isSprinting()) {
            if (blockadeDist == 2) return false; // ONE LESS THAN SPRINTING JUMP DISTANCE
            else if (blockadeDist > 2 && npc.isJumping()) return true; // ONE LESS THAN SPRINTING JUMP DISTANCE
        }
        if (npc.isOnGround() && groundTicks >= 4) {
            if (npc.isSprinting() ) {
                return blockadeDist <= 1; // SPRINTING JUMP DISTANCE
            } else {
                return blockadeDist <= 1; // WALKING JUMP DISTANCE
            }
        }
        return false;
    }

    public PathFinder getPathfinder() {
        return pathfinder;
    }

    public Path getCurrentPath() {
        return path;
    }

    public Path moveTo(Location location) {
        return moveTo(location, null);
    }

    public Path moveTo(Location location, Path path) {
        this.path = path;
        Location groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        Location npcGroundLoc = LocationUtils.getBlockInDir(npc.getLocation(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        goal = location.clone();
        if ((!options.straightLineInWater || !npc.isInWater()) && !options.straightLine) {
            if (this.path == null) this.path = pathfinder.getPath(BlockPos.fromLocation(npcGroundLoc), BlockPos.fromLocation(groundLoc), groundLoc.getWorld());
            if (this.path != null) this.path.clean();
        } else this.path = null;
        if (!isDone()) {
            Vector nextNPCPos = npc.isInWater() && options.straightLineInWater ? goal.toVector() : this.path.getNextNPCPos(npc);
            npc.getMoveController().moveTo(nextNPCPos.getX(), nextNPCPos.getZ());
        }
        return this.path;
    }

    private void followPath() {
        Vector tempNPCPos = getTempNPCPos();
        double maxDistToWaypoint = 0.45; // 0.52
        Vector blockPos = path.getNextNodePos();
        Block block = new Location(npc.getLocation().getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ()).getBlock();
        double x = Math.abs(npc.getLocation().getX() - blockPos.getX() + 0.5);
        double y = Math.abs(npc.getLocation().getY() - blockPos.getY());
        double z = Math.abs(npc.getLocation().getZ() - blockPos.getZ() + 0.5);
        boolean withinMaxDist = (x < maxDistToWaypoint && z < maxDistToWaypoint && y < 1D);
        if (withinMaxDist || (canCutCorner(block.getType()) && shouldTargetNextNode(tempNPCPos))) {
            path.advance();
            Vector nextNPCPos = path.getNextNPCPos(npc);
            npc.getMoveController().moveTo(nextNPCPos.getX(), nextNPCPos.getZ());
        }
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
        if (!LocationUtils.closerThan(tempNPCPos, center, 2)) return false; // was 2
        Vector nextCenter = LocationUtils.atBottomCenterOf(path.getNodePos(path.getNextNodeIndex() + 1));
        Vector nextNodeDiff = nextCenter.subtract(center);
        Vector tempPosDiff = tempNPCPos.subtract(center);
        return nextNodeDiff.dot(tempPosDiff) > 0;
    }

    private Vector getTempNPCPos() {
        return npc.getLocation().toVector();
    }

    private boolean canUpdatePath() {
        return !isDone();
    }

    private boolean isDone() {
        return path == null || path.isDone();
    }

    public void stop() {
        path = null;
    }

    public Options getOptions() {
        return options;
    }

    public static class Options {
        private float maxStepHeight = 0.3F;
        private boolean straightLine;
        private boolean straightLineInWater = true;
        private boolean showPath;

        public float getMaxStepHeight() {
            return maxStepHeight;
        }

        public void setMaxStepHeight(float maxStepHeight) {
            this.maxStepHeight = maxStepHeight;
        }

        public boolean isStraightLine() {
            return straightLine;
        }

        public void setStraightLine(boolean straightLine) {
            this.straightLine = straightLine;
        }

        public boolean isStraightLineInWater() {
            return straightLineInWater;
        }

        public void setStraightLineInWater(boolean straightLineInWater) {
            this.straightLineInWater = straightLineInWater;
        }

        public boolean isShowPath() {
            return showPath;
        }

        public void setShowPath(boolean showPath) {
            this.showPath = showPath;
        }
    }
}
