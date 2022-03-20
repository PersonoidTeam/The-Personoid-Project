package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.ai.pathfinding.requirements.PathRequirement;
import com.personoid.utils.debug.Profiler;
import com.personoid.utils.debug.Timer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Pathfinder {
    private World world;
    private List<PathRequirement> requirements;

    public World getWorld() {
        return world;
    }

    public List<PathRequirement> getRequirements() {
        return requirements;
    }

    public Path findPath(Location from, Location to, List<PathRequirement> requirements, int maxNodes, int maxNodesUntilRetry) {
        Profiler.push(Profiler.Type.A_STAR, "Calculating path...");
        world = from.getWorld();
        this.requirements = requirements;

        PathNode start = new PathNode(this, from);
        //Location goalTargetLoc = npc.getDataContainer().retrieve("goalTargetLoc");
        //PathNode end = new PathNode(this, LocationUtils.getBlockInDir(goalTargetLoc, BlockFace.DOWN).getLocation());
        PathNode end = new PathNode(this, to);

        Queue<PathNode> open = new PriorityQueue<>(List.of(start));
        Set<PathNode> closed = new HashSet<>();
        ArrayList<PathNode> navigated = new ArrayList<>();
        start.f = start.distanceTo(end);

        Timer retryTimer = new Timer().start();

        while (!open.isEmpty()) {
            PathNode current = open.poll();
            if (current.distanceTo(end) < 1 || (navigated.size() >= maxNodes && maxNodes != -1)) {
                navigated.add(navigated.size() < maxNodes ? end : current);
                Path path = reconstruct(navigated, navigated.size() - 1);
                Profiler.push(Profiler.Type.A_STAR, "Total time spent (path): " + retryTimer.get() + "ms");
                return path;
            }
            open.remove(current);
            closed.add(current);
            for (PathNode neighbour : current.getNeighbours()) {
                if (closed.contains(neighbour)) continue;
                double tentG = current.g + current.distanceTo(neighbour);
                if (!open.contains(neighbour) || tentG < neighbour.g) {
                    if (!navigated.contains(current)) {
                        navigated.add(current);
                    }
                    neighbour.g = tentG;
                    neighbour.h = neighbour.distanceTo(end);
                    neighbour.f = tentG + neighbour.h;
                    if (!open.contains(neighbour)) {
                        open.add(neighbour);
                    }
                }
            }
            if (maxNodesUntilRetry != -1 && retryTimer.get() > maxNodesUntilRetry && requirements.size() > 0) {
                Profiler.push(Profiler.Type.A_STAR, "Took too long to calculate, retrying with no requirements...");
                return findPath(from, to, new ArrayList<>(), maxNodes, maxNodesUntilRetry);
            }
        }
        Profiler.push(Profiler.Type.A_STAR, "Total time spent (null): " + retryTimer.get() + "ms");
        return null;
    }

    private Path reconstruct(List<PathNode> navigated, int index) {
        final PathNode current = navigated.get(index);
        Path path = new Path(current);
        if (index > 0 && navigated.contains(current)) {
            return reconstruct(navigated, index - 1).append(path.nodes());
        }
        path.nodes().remove(0);
        return path;
    }
}
