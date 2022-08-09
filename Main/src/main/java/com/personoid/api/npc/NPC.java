package com.personoid.api.npc;

import com.personoid.api.ai.NPCBrain;
import com.personoid.api.ai.looking.LookController;
import com.personoid.api.ai.movement.MoveController;
import com.personoid.api.ai.movement.Navigation;
import com.personoid.api.utils.types.HandEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public interface NPC {
    Navigation getNavigation();
    MoveController getMoveController();
    LookController getLookController();
    BlockBreaker getBlockBreaker();
    PlayerInventory getNPCInventory();
    NPCBrain getNPCBrain();

    void tick();

    Location getLocation();
    double getXPos();
    double getYPos();
    double getZPos();
    Player getEntity();
    boolean onGround();
    boolean inWater();
    int getGroundTicks();
    void setGroundTicks(int ticks);

    void remove();
    void setSneaking(boolean sneaking);
    void setSwimming(boolean swimming);
    void showToPlayers(Player... players);
    void hideToPlayers(Player... players);
    void move(Vector velocity);

    void setYRotation(float yRot);
    float getYRotation();
    void swingHand(HandEnum hand);
    void setSkin(Skin skin);
}
