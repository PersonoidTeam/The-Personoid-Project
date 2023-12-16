package com.personoid.api.pathfindingold.movement.helpers;

import com.personoid.api.npc.NPC;
import com.personoid.api.pathfindingold.BlockPos;
import com.personoid.api.pathfindingold.movement.Movement;
import com.personoid.api.pathfindingold.targets.Target;

import java.util.ArrayList;
import java.util.List;

public class TargetHelper<T extends Target> extends Helper {
    private final List<T> targets = new ArrayList<>();

    public TargetHelper(NPC npc, Movement movement) {
        super(npc, movement);
    }

    public void addTarget(T target) {
        targets.add(target);
    }

    public void removeTarget(T target) {
        targets.remove(target);
    }

    public boolean hasTarget(T target) {
        return targets.contains(target);
    }

    public boolean hasTarget(BlockPos pos) {
        for (T target : targets) {
            if (target.getPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTarget(int x, int y, int z) {
        for (T target : targets) {
            if (target.getPos().equals(new BlockPos(x, y, z))) {
                return true;
            }
        }
        return false;
    }

    public Target getTarget(BlockPos pos) {
        for (T target : targets) {
            if (target.getPos().equals(pos)) {
                return target;
            }
        }
        return null;
    }

    public T getTarget(int x, int y, int z) {
        for (T target : targets) {
            if (target.getPos().equals(new BlockPos(x, y, z))) {
                return target;
            }
        }
        return null;
    }

    public boolean hasTargets() {
        return !targets.isEmpty();
    }

    public List<T> getTargets() {
        return targets;
    }
}
