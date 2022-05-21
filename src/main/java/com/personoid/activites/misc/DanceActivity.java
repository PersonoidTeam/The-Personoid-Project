package com.personoid.activites.misc;

import com.personoid.npc.ai.Priority;
import com.personoid.npc.ai.activity.Activity;
import com.personoid.npc.ai.activity.ActivityType;
import com.personoid.utils.MathUtils;
import net.minecraft.world.InteractionHand;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class DanceActivity extends Activity {
    private int nextToggleCrouchTick;
    private int nextPunchTick;
    private int nextLookTick;

    public DanceActivity() {
        super(ActivityType.IDLING, Priority.LOWEST, new BoredomSettings(MathUtils.random(200, 1200), MathUtils.random(2400, 12000)));
    }

    @Override
    public void onStart(StartType startType) {

    }

    @Override
    public void onUpdate() {
        if (nextToggleCrouchTick <= 0) {
            nextToggleCrouchTick = MathUtils.random(2, 10);
            getActiveNPC().setSneaking(!getActiveNPC().isSneaking());
        }
        if (nextPunchTick <= 0) {
            nextPunchTick = MathUtils.random(2, 10);
            getActiveNPC().swing(InteractionHand.MAIN_HAND);
        }
        if (nextLookTick <= 0) {
            nextLookTick = MathUtils.random(5, 15);
            Vector randomVec = new Vector(MathUtils.random(-5, 5), MathUtils.random(-5, 5), MathUtils.random(-5, 5));
            Location facing = getActiveNPC().getLocation().clone().add(randomVec);
            getActiveNPC().getLookController().face(facing);
        }
        nextToggleCrouchTick--;
        nextPunchTick--;
        nextLookTick--;
    }

    @Override
    public void onStop(StopType stopType) {
        getActiveNPC().setSneaking(false);
        getActiveNPC().getLookController().forget();
    }

    @Override
    public boolean canStart(StartType startType) {
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }
}
