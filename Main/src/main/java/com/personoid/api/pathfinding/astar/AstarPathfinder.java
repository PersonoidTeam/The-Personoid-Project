package com.personoid.api.pathfinding.astar;

import com.personoid.api.pathfinding.Node;
import com.personoid.api.pathfinding.Path;
import com.personoid.api.pathfinding.Pathfinder;
import com.personoid.api.pathfinding.PathingContext;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.debug.Profiler;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class AstarPathfinder extends Pathfinder {
    @Override
    public Path getPath(Location start, Location end) {
        PathingContext context = new PathingContext(start, end, options);
        boolean pathFound = options.canUseChunking();

        //if (!(canStandAt(start) && canStandAt(end))) return null;

        long nsStart = System.nanoTime();
        double bestExpense = Double.MAX_VALUE;

        while (context.getCheckedNodes().size() < options.getMaxNodeTests() && context.getUncheckedNodes().size() > 0) {
            Node best =  context.getUncheckedNodes().get(0);
            for (Node node : context.getUncheckedNodes()) {
                if (node.getEstimatedFinalExpense() < best.getEstimatedFinalExpense()) {
                    best = node;
                }
            }
            if (options.canUseChunking() && best.getEstimatedExpenseLeft() < bestExpense) {
                context.setEndNode(best);
                bestExpense = best.getEstimatedExpenseLeft();
            }
            if (best.getEstimatedExpenseLeft() < 1) {
                pathFound = true;
                context.setEndNode(best);
                Profiler.PATHFINDING.push("Unchecked: " + context.getUncheckedNodes().size() + ", Checked: " +
                        context.getCheckedNodes().size() + ", Expense: " + MathUtils.round(best.getExpense(), 2) +
                        ", Final Expense: " + MathUtils.round(best.getEstimatedFinalExpense(), 2));
                break;
            }
            best.getReachableLocations();
            context.getUncheckedNodes().remove(best);
            context.getCheckedNodes().add(best);
        }

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
        for (int i = length - 1; i > 0; i--) {
            nodes[i] = node;
            node = node.getOrigin();
        }
        nodes[0] = context.getStartNode();

        // outputting benchmark result
        float duration = (System.nanoTime() - nsStart) / 1000000f;
        Profiler.PATHFINDING.push("A* took " + (duration > 50 ? ChatColor.RED : ChatColor.WHITE) +
                duration + "ms" + ChatColor.WHITE + " to find a path.");
        return new Path(nodes);
    }
}
