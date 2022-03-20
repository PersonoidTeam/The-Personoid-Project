package com.notnotdoddy.personoid.npc.ai.pathfinding;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.NPCComponent;
import com.notnotdoddy.personoid.npc.ai.pathfinding.goals.PathfinderGoal;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class PathTask extends NPCComponent {
    private final Path path;
    private final PathfinderGoal goal;
    private int nodeIndex;
    Vector lastTravelingVector = null;

    public PathTask(NPC npc, Path path) {
        super(npc);
        this.path = path;
        goal = npc.getGoalSelector().getCurrentGoal();
    }

    public Vector getVelocity() {
        if (npc.getLocation().distance(path.getEndLocation()) < goal.getData().getStoppingDistance()) {
            lastTravelingVector = null;
            return new Vector();
        }
        Vector velocity = getTargetNode().getLocation().clone().subtract(npc.getLocation()).toVector().normalize();
        lastTravelingVector = velocity;
        return velocity;
    }

    public Path getPath() {
        return path;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void update() {
        if (path.getNode(nodeIndex).getLocation().distanceSquared(npc.getLocation()) < 1F) {
            nodeIndex = Math.min(nodeIndex + 1, path.nodes().size() - 1);
        }
    }

    private PathNode getTargetNode() {
        return path.getNode(nodeIndex);
    }

    public boolean shouldJump() {
        return shouldJump(nodeIndex);
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
