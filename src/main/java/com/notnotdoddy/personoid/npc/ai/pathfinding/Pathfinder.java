package com.notnotdoddy.personoid.npc.ai.pathfinding;

import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.npc.NPCComponent;
import com.notnotdoddy.personoid.npc.ai.pathfinding.goals.PathfinderGoal;
import com.notnotdoddy.personoid.npc.ai.pathfinding.requirements.PathRequirement;
import com.notnotdoddy.personoid.utils.debug.Profiler;
import com.notnotdoddy.personoid.utils.debug.Timer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class Pathfinder extends NPCComponent {
    private List<PathRequirement> requirements;
    public Set<Block> closedBlocks = new HashSet<>();

    public Pathfinder(NPC npc) {
        super(npc);
    }

    public List<PathRequirement> getRequirements() {
        return requirements;
    }

    public PathNode toNode(Location location) {
        return new PathNode(this, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    // Credit to James Norris for open sourcing MCPath!
    // Made heavy optimisations to speed up calculations and integrated goal + requirements system

    public Path calculate(PathfinderGoal goal, List<PathRequirement> requirements, int maxNodes, int maxTicksUntilRetry) {
        this.requirements = requirements;
        closedBlocks.clear();
        Profiler.push(Profiler.Type.A_STAR, "calculating path...");
        Location endLoc;
        // TODO: change to check down until it finds a block, not just the first one
        if (!goal.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            endLoc = goal.getLocation().clone().subtract(0, 1, 0);
        } else endLoc = goal.getLocation().clone();
        PathNode start = toNode(npc.getLocation());
        PathNode end = toNode(endLoc);

        Queue<PathNode> open = new PriorityQueue<>() {{ add(start); }};
        Set<PathNode> closed = new HashSet<>();
        ArrayList<PathNode> navigated = new ArrayList<>();
        start.setF(start.distance(end));

        Timer retryTimer = new Timer().start();

        while (!open.isEmpty()) {
            PathNode current = open.poll();
            if (current.distance(end) < 1 || (navigated.size() >= maxNodes && maxNodes != -1)) {
                navigated.add(navigated.size() < maxNodes ? end : current);
                Path path = reconstruct(navigated, navigated.size() - 1);
                Profiler.push(Profiler.Type.A_STAR, "Total time spent (path): " + retryTimer.get() + "ms");
                return path;
            }
            open.remove(current);
            closed.add(current);
            for (PathNode neighbour : current.getNeighbors()) {
                if (closed.contains(neighbour)) {
                    continue;
                }
                double tentG = current.getG() + current.distance(neighbour);
                if (!open.contains(neighbour) || tentG < neighbour.getG()) {
                    if (!navigated.contains(current)) {
                        navigated.add(current);
                    }
                    neighbour.setG(tentG);
                    neighbour.setH(neighbour.distance(end));
                    neighbour.setF(tentG + neighbour.getH());
                    if (!open.contains(neighbour)) {
                        open.add(neighbour);
                    }
                }
            }
            if (maxTicksUntilRetry != -1 && retryTimer.get() > maxTicksUntilRetry && requirements.size() > 0) {
                Profiler.push(Profiler.Type.A_STAR, "Took too long to calculate, retrying with no requirements...");
                return calculate(goal, new ArrayList<>(), maxNodes, 0);
            }
        }
        Profiler.push(Profiler.Type.A_STAR, "Total time spent (null): " + retryTimer.get() + "ms");
        return null;
    }

    private Path reconstruct(List<PathNode> navigated, int index) {
        final PathNode current = navigated.get(index);
        Path path = new Path(new ArrayList<>() {{ add(current); }});
        if (index > 0 && navigated.contains(current)) {
            return reconstruct(navigated, index - 1).merge(path);
        }
        path.clean();
        return path;
    }
}
