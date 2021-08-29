package us.notnotdoddy.personoid.npc;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class TargetHandler {

    public enum TargetType {
        FOLLOW_LIVING_ENTITY,
        ATTACK_LIVING_ENTITY,
        BLOCK,
        NOTHING
    }


    // This is for future proofing. If the NPCs need to interact with blocks we shouldnt have to band-aid fix block interactions
    public static void setBlockTarget(PersonoidNPC personoidNPC, Block block){
        personoidNPC.citizen.getNavigator().setTarget(block.getLocation());
        personoidNPC.activeTargetType = TargetType.BLOCK;
    }

    // See reason above for separation
    public static void setLivingEntityTarget(PersonoidNPC personoidNPC, LivingEntity livingEntity, boolean attack){
        personoidNPC.citizen.getNavigator().setTarget(livingEntity, false);
        if (attack){
            personoidNPC.activeTargetType = TargetType.ATTACK_LIVING_ENTITY;
        }
        else {
            personoidNPC.activeTargetType = TargetType.FOLLOW_LIVING_ENTITY;
        }
        personoidNPC.setLivingEntityTarget(livingEntity);
        personoidNPC.setCurrentTargetLocation(livingEntity.getLocation());
    }

    public static void setNothingTarget(PersonoidNPC personoidNPC, Location location){
        personoidNPC.citizen.getNavigator().setTarget(location);
        personoidNPC.citizen.getNavigator().getLocalParameters().straightLineTargetingDistance(0);
        personoidNPC.activeTargetType = TargetType.NOTHING;
        personoidNPC.setCurrentTargetLocation(location);
    }

}
