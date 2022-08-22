package com.personoid.api.ai.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.Node;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.pathfinding.Pathfinder;
import com.personoid.api.pathfinding.astar.AstarPathfinder;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.math.MathUtils;
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
    private final Pathfinder pathfinder = new AstarPathfinder();
    private final Options options = new Options();
    private Path path;

    public Navigation(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        if (isDone()) return;
        if (canUpdatePath()) {
            followPath();
        } else if (path != null && !path.isDone()) {
            Vector tempNPCPos = getTempNPCPos();
            Vector nextNPCPos = path.getNextNPCPos(npc);
            if (tempNPCPos.getY() > nextNPCPos.getY() && !npc.onGround() && Math.floor(tempNPCPos.getX()) == Math.floor(nextNPCPos.getX()) &&
                    Math.floor(tempNPCPos.getZ()) == Math.floor(nextNPCPos.getZ())) {
                Profiler.NAVIGATION.push("advancing to next node 1");
                path.advance();
            }
        }
        if (isDone()) return;

        // movement
        Vector nextNPCPos = path.getNextNPCPos(npc);
        Location nextLoc = new Location(npc.getLocation().getWorld(), nextNPCPos.getX(), nextNPCPos.getY(), nextNPCPos.getZ());
        Vector velocity = new Vector(nextLoc.getX() - npc.getXPos(), nextLoc.getY() - npc.getYPos(), nextLoc.getZ() - npc.getZPos());
        Vector lerpedVelocity = MathUtils.lerpVector(npc.getMoveController().getVelocity(), velocity, options.getMovementSmoothing());
        lerpedVelocity.setY(velocity.getY());
        npc.getMoveController().move(lerpedVelocity, options.movementType);

        // check if next npc pos is one block up from current
        if (options.movementType == MovementType.SPRINT_JUMPING) {
            int count = 0;
            for (int i = 0; i < 3; i++) {
                Vector nextIndexNPCPos = path.getNPCPosAtNode(npc, path.getNextNodeIndex() + i);
                if (nextIndexNPCPos.getY() > npc.getYPos()) {
                    count++;
                }
            }
            if (count == 0) {
                npc.getMoveController().jump();
                Profiler.NAVIGATION.push("jumped (sprint jumping)");
            }
        }

        // movement specifics
        if (nextNPCPos.getY() >= npc.getYPos() + options.getMaxStepHeight()) {
            Block blockDown = nextLoc.getBlock().getRelative(BlockFace.DOWN);
            if (npc.onGround()) {
                if (blockDown.getType().name().contains("STAIRS")) {
                    npc.getMoveController().step(((nextNPCPos.getY() - npc.getYPos()) / 2) * 1.2F); // move up to half stair height
                    Profiler.NAVIGATION.push("stepping on stairs");
                } else if (BlockTags.CLIMBABLE.is(blockDown.getType())) {
                    npc.getMoveController().step(0.2F);
                    Profiler.NAVIGATION.push("climbing");
                } else if (path.getNPCPosAtNode(npc, path.getNextNodeIndex() + 1).getY() > npc.getYPos() + options.getMaxStepHeight()) {
                    if (npc.getGroundTicks() >= 4) {
                        npc.getMoveController().jump();
                        Profiler.NAVIGATION.push("jumped");
                    }
                }
            }
        } else {
            double diff = nextNPCPos.getY() - npc.getYPos();
            if (diff > 0 && diff < options.getMaxStepHeight()) {
                npc.getMoveController().step((nextNPCPos.getY() - npc.getYPos()) * 1.2F);
            }
        }
        if (path != null) {
            for (Node node : path.getNodes()) {
                npc.getEntity().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation().clone().add(0.5F, 0, 0.5F), 5,
                        new Particle.DustTransition(Color.YELLOW, Color.RED, 1));
            }
        }
        //trimPath();
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public void moveTo(Location location, MovementType movementType) {
        options.movementType = movementType;
        Location groundLoc = LocationUtils.getBlockInDir(location, BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        Location npcGroundLoc = LocationUtils.getBlockInDir(npc.getLocation(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
        // FIXME: async leads to errors (concurrent modification exception) -> should be fast enough or synchronous running anyway
        path = pathfinder.getPath(npcGroundLoc, groundLoc);
        if (path != null) Profiler.NAVIGATION.push("found path, length: " + path.size());
    }

    private void followPath() {
        Vector tempNPCPos = getTempNPCPos();
        double maxDistToWaypoint = (npc.getEntity().getWidth() > 0.75) ? (npc.getEntity().getWidth() / 2) : (0.75 - npc.getEntity().getWidth() / 2);
        Vector blockPos = path.getNextNodePos();
        Block block = new Location(npc.getLocation().getWorld(), blockPos.getX(), blockPos.getY(), blockPos.getZ()).getBlock();
        double x = Math.abs(npc.getXPos() - blockPos.getX() + 0.5);
        double y = Math.abs(npc.getYPos() - blockPos.getY());
        double z = Math.abs(npc.getZPos() - blockPos.getZ() + 0.5);
        boolean withinMaxDist = (x < maxDistToWaypoint && z < maxDistToWaypoint && y < 1D); //y < 1D
        if (withinMaxDist || (canCutCorner(block.getType()) && shouldTargetNextNode(tempNPCPos))) {
            Profiler.NAVIGATION.push("advancing to next node 2");
            this.path.advance();
        }
        //doStuckDetection(tempNPCPos);
    }

    private boolean canCutCorner(Material material) {
        return !material.name().contains("FIRE") && !material.name().contains("CACTUS") && !material.name().contains("DOOR") &&
                !material.name().contains("LAVA") && !material.name().contains("COBWEB");
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
        Profiler.NAVIGATION.push("can update path: " + npc.onGround());
        return (npc.onGround()); // TODO: or if in liquid
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
        private float movementSmoothing = 0.1F;
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
