package com.notnotdoddy.personoid.npc.ai.pathfinding;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.NPCTickingComponent;
import com.notnotdoddy.personoid.npc.ai.pathfinding.requirements.WalkablePathRequirement;
import com.notnotdoddy.personoid.utils.debug.Profiler;
import com.notnotdoddy.personoid.utils.task.Task;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.List;

public class Navigation extends NPCTickingComponent {
    private final GoalSelector goalSelector;
    private final Pathfinder pathfinder;
    private Path path;

    private int nodeIndex;
    Vector lastTravelingVector = null;

    public Navigation(NPC npc, GoalSelector goalSelector) {
        super(npc);
        this.goalSelector = goalSelector;
        this.pathfinder = new Pathfinder(npc);
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    @Override
    public void tick() {
        super.tick();
        Profiler.push(Profiler.Type.NAVIGATION, (goalSelector.getCurrentGoal() != null) + " goal");
        Profiler.push(Profiler.Type.NAVIGATION, (path != null) + " pathtask");
        if (currentTick % 10 == 0 && goalSelector.getCurrentGoal() != null) {
            new Task(() -> {
                if (path == null){
                    path = pathfinder.calculate(goalSelector.getCurrentGoal(), List.of(new WalkablePathRequirement()), -1, 500);
                }
                Path tempPath = pathfinder.calculate(goalSelector.getCurrentGoal(), List.of(new WalkablePathRequirement()), -1, 500);
                if (tempPath != null) {
                    int sizeOfPathWithMostNodes = Math.max(tempPath.nodes().size(), path.nodes().size());
                    for (int i = 0; i < sizeOfPathWithMostNodes; i++) {
                        PathNode pathNode = path.nodes().get(i);
                        PathNode tempPathNode = tempPath.nodes().get(i);
                        if (i > path.nodes().size() - 1) {
                            path.append(tempPathNode);
                            continue;
                        }
                        if (pathNode.getLocation() != tempPathNode.getLocation()) {
                            path.nodes().set(i, tempPathNode);
                        }
                    }
                }
            }).async().run();
        }
        else {
            if (path != null) {
                if (lastTravelingVector != null) {
                    npc.walk(lastTravelingVector);
                }
            }
        }
        if (path != null) {
            npc.walk(getVelocity());
            if (shouldJump()) {
                npc.jump();
            }
            update();

            // debugging
            Profiler.push(Profiler.Type.NAVIGATION, "pathNodeIndex: " + nodeIndex);

            for (PathNode node : path.nodes()) {
                npc.getBukkitEntity().getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, node.getLocation(), 5,
                        new Particle.DustTransition(Color.YELLOW, Color.RED, 1));
            }
        }
    }

    public Vector getVelocity() {
        if (npc.getLocation().distance(path.getEndLocation()) < npc.getGoalSelector().getCurrentGoal().getData().getStoppingDistance()) {
            lastTravelingVector = null;
            return new Vector();
        }
        Vector velocity = getTargetNode().getLocation().clone().subtract(npc.getLocation()).toVector().normalize();
        lastTravelingVector = velocity;
        return velocity;
    }

    public void update() {
        if (path.getNode(nodeIndex).getLocation().distanceSquared(npc.getLocation()) < 1F) {
            nodeIndex = Math.min(nodeIndex + 1, path.nodes().size() - 1) - 1;
            path.clean();
        }
    }

    private PathNode getTargetNode() {
        return path.getNode(nodeIndex);
    }

    public boolean shouldJump() {
        if (path.nodes().size() > 1 || nodeIndex == path.nodes().size() - 1) return false;
        PathNode node = path.getNode(nodeIndex);
        PathNode nextNode = path.getNode(nodeIndex + 1);
        return nextNode.getLocation().getBlockY() - node.getLocation().getBlockY() == 1;
    }

    public boolean shouldJump(int atNodeIndex) {
        PathNode atNode = path.getNode(atNodeIndex);
        Location atLoc = atNode.getLocation();
        if (atLoc.distance(npc.getLocation()) > 1) return false;
        Block atBlock = atLoc.getBlock().getRelative(BlockFace.DOWN);
        if (atBlock.getType().isSolid()) {
            for (int i = atNodeIndex + 1; i < path.nodes().size(); i++) {
                PathNode currentNode = path.getNode(i);
                Location currentLoc = currentNode.getLocation();
                if (atLoc.distance(currentLoc) > 3) continue;
                Block currentBlock = currentLoc.getBlock().getRelative(BlockFace.DOWN);
                if (npc.isOnGround() && atLoc.getBlockY() + 1 == currentLoc.getBlockY() && currentBlock.getType().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}
