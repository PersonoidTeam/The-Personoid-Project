package com.personoid.api.pathfindingold.movement;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.Node;
import com.personoid.api.pathfindingold.Pathfinder;

import java.util.ArrayList;
import java.util.List;

public abstract class Move {
    private static final List<Move> MOVES = new ArrayList<>();

    private final int dx;
    private final int dy;
    private final int dz;

    public Move(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;

        MOVES.add(this);
    }

    public abstract Movement apply(NPC npc, Node node, Pathfinder finder);

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public int getDeltaZ() {
        return dz;
    }

    public static List<Move> getMoves() {
        return MOVES;
    }
}
