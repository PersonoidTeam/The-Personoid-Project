package com.notnotdoddy.personoid.npc.ai.pathfinding.goals;

import com.notnotdoddy.personoid.npc.NPC;
import org.bukkit.entity.LivingEntity;

public class FollowEntityGoal<T extends LivingEntity> extends PathfinderGoal {
    private final T target;

    public FollowEntityGoal(NPC npc, T target) {
        super(npc, Priority.LOW);
        this.target = target;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onUpdate() {
        setTargetLocation(target.getLocation());
        if (target.getLocation().distance(npc.getLocation()) < 20) {
            setFacingLocation(target.getLocation());
        } else {
            setFacingLocation(getNextNodeLocation().add(0, 1.5F, 0));
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean canStart(StartInfo info) {
        return true;
    }

    @Override
    public boolean canStop(StopInfo info) {
        return true;
    }
}
