package us.notnotdoddy.personoid.goals.defense;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.notnotdoddy.personoid.goals.NPCGoal;
import us.notnotdoddy.personoid.npc.MovementType;
import us.notnotdoddy.personoid.npc.NPCTarget;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.status.Behavior;

public class AttackMeanPlayersGoal extends NPCGoal {
    private final int botRange = 20;

    public AttackMeanPlayersGoal() {
        super(true, GoalPriority.HIGH);
    }

    @Override
    public void initializeGoal(PersonoidNPC npc) {
        npc.sendMessage(Behavior.Mood.ANGRY, "attack-player");
        npc.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
        npc.target(new NPCTarget(npc.getClosestPlayer(), NPCTarget.EntityTargetType.ATTACK).setMovementType(MovementType.SPRINT_JUMPING).setStraightness(100));
    }

    @Override
    public void endGoal(PersonoidNPC npc) {
        npc.sendMessage(Behavior.Mood.ANGRY, "attack-player-end");
        npc.setItemInMainHand(null);
        npc.forgetTarget();
    }

    @Override
    public boolean canStart(PersonoidNPC npc) {
        //Bukkit.broadcastMessage(personoidNPC.players.get(personoidNPC.getClosestPlayerToNPC()).getMoodValue(Behavior.Mood.ANGRY) + "");
        return npc.isTarget(npc.getClosestPlayer()) && npc.getClosestPlayer().getLocation().distance(npc.getEntity().getLocation()) <= botRange;
    }

    @Override
    public void tick(PersonoidNPC npc) {
        if (npc.getEntityTarget().getLocation().distance(npc.getEntity().getLocation()) < 3){
            npc.hitTarget(npc.getEntityTarget(), 6, 20);
        }
    }

    @Override
    public boolean shouldStop(PersonoidNPC npc) {
        return npc.getEntity().getLocation().distance(npc.getEntityTarget().getLocation()) > botRange + 5;
    }
}
