package us.notnotdoddy.personoid.npc;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class TargetHandler {

    public enum TargetType {
        FOLLOW_LIVING_ENTITY,
        ATTACK_LIVING_ENTITY,
        BLOCK_BREAK,
        BLOCK_INTERACT,
        NOTHING
    }


    // This is for future proofing. If the NPCs need to interact with blocks we shouldnt have to band-aid fix block interactions
    public static void setBlockTarget(PersonoidNPC personoidNPC, Block block, boolean destroy){
        // We will have a system for switching between straight line and actual pathfinding in the future.
        // For now, this works.
        if (personoidNPC.citizen.getNavigator().isNavigating()) {
            personoidNPC.citizen.getNavigator().cancelNavigation();
        }
        personoidNPC.citizen.getNavigator().setTarget(block.getLocation());
        personoidNPC.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(100);
        personoidNPC.setTargetLocation(block.getLocation());
        personoidNPC.data.targetType = destroy ? TargetType.BLOCK_BREAK : TargetType.BLOCK_INTERACT;
    }

    // See reason above for separation
    public static void setLivingEntityTarget(PersonoidNPC personoidNPC, LivingEntity livingEntity, boolean attack){
        personoidNPC.citizen.getNavigator().setTarget(livingEntity, false);
        if (attack){
            personoidNPC.data.targetType = TargetType.ATTACK_LIVING_ENTITY;
        }
        else {
            personoidNPC.data.targetType = TargetType.FOLLOW_LIVING_ENTITY;
        }
        personoidNPC.setEntityTarget(livingEntity);
        personoidNPC.setTargetLocation(livingEntity.getLocation());
    }

    public static void setNothingTarget(PersonoidNPC personoidNPC, Location location) {
        if (personoidNPC.citizen.getNavigator().isNavigating()) {
            personoidNPC.citizen.getNavigator().cancelNavigation();
        }
        personoidNPC.citizen.getNavigator().setTarget(location);
        personoidNPC.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(0);
        personoidNPC.setTargetLocation(location);
    }
}
