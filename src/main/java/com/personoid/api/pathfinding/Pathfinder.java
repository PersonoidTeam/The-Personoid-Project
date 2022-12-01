package com.personoid.api.pathfinding;

import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.math.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Pathfinder {
    protected PathingConfig config = new PathingConfig();

    public Path getPath(Location start, Location end) {
        PathingContext context = new PathingContext(start, end, config);
        boolean pathFound = config.canUseChunking();

        //if (!(canStandAt(start) && canStandAt(end))) return null;

        long nsStart = System.nanoTime();
        Node best = null;

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
        Node node = context.getEndNode();
        while (node.getOrigin() != null) {
            node = node.getOrigin();
            length++;
        }

        Node[] nodes = new Node[length];

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
        return new Path(nodes);
    }

    public PathingConfig getConfig() {
        return config;
    }

    public void setConfig(PathingConfig config) {
        this.config = config;
    }
}
