package com.personoid.api.pathfinding;

import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.math.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;

public class PathFinder {
    protected PathingConfig config = new PathingConfig();

    public Path getPath(BlockPos start, BlockPos end, World world) {
        PathingContext context = new PathingContext(start, end, world, config);
        boolean pathFound = config.canUseChunking();

        //if (!(canStandAt(start) && canStandAt(end))) return null;

        long nsStart = System.nanoTime();
        PathingNode best = null;

        while (context.getClosedSet().size() < config.getMaxNodeTests() && context.getOpenSet().size() > 0) {
            best = context.getOpenSet().poll();
            if (best.getExpenseLeft() < 1) {
                pathFound = true;
                context.setEndNode(best);
                Profiler.PATHFINDING.push("Unchecked: " + context.getOpenSet().size() + ", Checked: " +
                        context.getClosedSet().size() + ", Expense: " + MathUtils.round(best.getExpense(), 2) +
                        ", Final Expense: " + MathUtils.round(best.getFinalExpense(), 2));
                break;
            }
            best.getReachableLocations();
            context.getClosedSet().add(best);
        }

        if (config.canUseChunking() && best != null) context.setEndNode(best);

        // returning if no path has been found
        if (!pathFound) {
            float duration = (System.nanoTime() - nsStart) / 1000000f;
            Profiler.PATHFINDING.push("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) +
                    duration + "ms" + ChatColor.WHITE + " to not find a path.");
            return null;
        }

        // get length of path to create array, 1 because of start
        int length = 1;
        PathingNode node = context.getEndNode();
        while (node.getOrigin() != null) {
            node = node.getOrigin();
            length++;
        }

        PathingNode[] nodes = new PathingNode[length];

        //fill Array
        node = context.getEndNode();
        for (int i = length - 1; i >= 0; i--) {
            nodes[i] = node;
            node = node.getOrigin();
        }
        //nodes[0] = context.getStartNode();

        // outputting benchmark result
        float duration = (System.nanoTime() - nsStart) / 1000000f;
        Profiler.PATHFINDING.push("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) +
                duration + "ms" + ChatColor.WHITE + " to find a path.");
        return nodes.length > 0 ? new Path(nodes) : null;
    }

    public PathingConfig getConfig() {
        return config;
    }

    public void setConfig(PathingConfig config) {
        this.config = config;
    }
}
