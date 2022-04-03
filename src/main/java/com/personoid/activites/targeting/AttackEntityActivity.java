package com.personoid.activites.targeting;

import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import org.bukkit.entity.LivingEntity;

public class AttackEntityActivity extends Activity {
    private final LivingEntity entity;

    public AttackEntityActivity(LivingEntity entity) {
        super(ActivityType.FIGHTING);
        this.entity = entity;
    }

    @Override
    public void onStart(StartType startType) {
        // TODO: should attack entity if they have better gear?
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    private boolean shouldAttack() {
        return true;
    }
}
