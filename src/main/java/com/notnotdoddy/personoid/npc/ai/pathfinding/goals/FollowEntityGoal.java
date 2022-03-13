package com.notnotdoddy.personoid.npc.ai.pathfinding.goals;

import com.notnotdoddy.personoid.npc.NPC;
import org.bukkit.entity.LivingEntity;

public class FollowEntityGoal<T extends LivingEntity> extends PathfinderGoal {
    private final T target;

    public FollowEntityGoal(NPC npc, T target) {
        super(npc, Type.PREFER, Priority.LOW, new Data().setStoppingDistance(1.5F));
        this.target = target;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onUpdate() {
        setLocation(target.getLocation());
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
