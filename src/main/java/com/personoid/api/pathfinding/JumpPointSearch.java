package com.personoid.api.pathfinding;

import com.personoid.api.pathfinding.node.Node;
import com.personoid.api.pathfinding.node.OldNode;
import com.personoid.api.pathfinding.utils.BlockPos;
import com.personoid.api.pathfinding.utils.Heuristics;

import java.util.ArrayList;
import java.util.List;

public class JumpPointSearch {
    public static Node[] getNeighbors(Node node, NodeContext context) {
        List<OldNode> neighbors = new ArrayList<>();
        for (BlockPos neighborPos : node.getNeighborPositions()) {
            Node neighborNode = new Node(neighborPos, context.getEnd(), context);
            if (neighborNode.isObstacle()) {
                continue;
            }

            Node jumpPoint = jump(node, neighborPos);
            if (jumpPoint != null) {
                Node jumpNode = new Node(jumpPoint, context.getEndPos(), context);
                neighbors.add(jumpNode);
            }
        }
        return neighbors;
    }

    private static Node jump(Node current, BlockPos target) {
        int dx = current.getDx();
        int dz = current.getDz();
        BlockPos position = current.getPosition();

        BlockPos next = position.add(dx, 0, dz);
        Node jumpNode = processSuccessor(current, next, target);

        if (jumpNode != null) {
            return jumpNode;
        }

        if (dx != 0) {
            next = position.add(dx, 0, -1);
            Node jumpNodeDiagonal = processSuccessor(current, next, target);
            if (jumpNodeDiagonal != null) {
                return jumpNodeDiagonal;
            }

            next = position.add(dx, 0, 1);
            jumpNodeDiagonal = processSuccessor(current, next, target);
            if (jumpNodeDiagonal != null) {
                return jumpNodeDiagonal;
            }
        }

        if (dz != 0) {
            next = position.add(-1, 0, dz);
            Node jumpNodeDiagonal = processSuccessor(current, next, target);
            if (jumpNodeDiagonal != null) {
                return jumpNodeDiagonal;
            }

            next = position.add(1, 0, dz);
            jumpNodeDiagonal = processSuccessor(current, next, target);
            if (jumpNodeDiagonal != null) {
                return jumpNodeDiagonal;
            }
        }

        if (dx != 0 && dz != 0) {
            if (jump(current.createNode(position.add(dx, 0, 0)), target) != null ||
                    jump(current.createNode(position.add(0, 0, dz)), target) != null) {
                return current;
            }
        }

        return null;
    }

    private static Node processSuccessor(Node current, BlockPos next, BlockPos target) {
        NodeContext context = current.getContext();

        if (!context.isWalkable(next)) {
            return null;
        }

        Node successor = current.createNode(next);
        double expense = current.getExpense() + Heuristics.euclidean(current.getPosition(), next);

        if (successor.getPosition().equals(target)) {
            successor.setFinalExpense(expense);
            return successor;
        }

        if (successor.getExpense() <= expense) {
            return null;
        }

        successor.setExpense(expense);
        successor.setFinalExpense(expense + Heuristics.euclidean(next, target));
        successor.setParent(current);

        if (successor.isDiagonal()) {
            int dx = successor.getDx();
            int dz = successor.getDz();
            BlockPos left = next.add(-dx, 0, 0);
            BlockPos right = next.add(0, 0, -dz);
            Node leftJump = jump(successor.createNode(left), next);
            Node rightJump = jump(successor.createNode(right), next);

            if (leftJump != null || rightJump != null) {
                successor.setJumpNode(leftJump != null ? leftJump : rightJump);
            }
        }

        return successor;
    }
}
