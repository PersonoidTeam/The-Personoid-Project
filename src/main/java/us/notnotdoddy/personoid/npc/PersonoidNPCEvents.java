package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidListener;
import me.definedoddy.fluidapi.FluidUtils;
import me.definedoddy.fluidapi.tasks.RepeatingTask;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.utils.PlayerInfo;

public class PersonoidNPCEvents {
    public static void init() {
        new FluidListener<>(NPCDeathEvent.class) {
            @Override
            public void run() {
                PersonoidNPC npc = PersonoidNPCHandler.getNPCs().get(getData().getNPC());
                npc.isFullyInitialized = false;
                npc.spawn(npc.spawnLocation);
                if (npc.damagedByPlayer != null) {
                    PlayerInfo info = npc.players.get(npc.damagedByPlayer);
                    npc.players.get(npc.damagedByPlayer).killedBy++;
                    info.incrementMoodStrength(Behavior.Mood.ANGRY, 0.25F);
                }
                setDelay(FluidUtils.random(20, 50));
            }
        }.setDelay(FluidUtils.random(20, 50));

        new FluidListener.Group() {
            @EventHandler
            public void damage(NPCDamageEvent e) {
                PersonoidNPC npc = PersonoidNPCHandler.getNPCs().get(e.getNPC());
                npc.damagedByPlayer = null;
            }

            @EventHandler
            public void damageByEntity(NPCDamageByEntityEvent e) {
                if (e.getDamager() instanceof Player player) {
                    PersonoidNPC npc = PersonoidNPCHandler.getNPCs().get(e.getNPC());
                    PlayerInfo info = npc.players.get(player.getUniqueId());
                    npc.damagedByPlayer = player;
                    info.incrementMoodStrength(Behavior.Mood.ANGRY, ((float) (1.1F * e.getDamage()))/10);
                }
            }
        };

        new RepeatingTask(0, 5, true) {
            @Override
            public void run() {
                for (PersonoidNPC personoidNPC : PersonoidNPCHandler.getNPCs().values()){
                    if (personoidNPC.isInHibernationState()){
                        boolean foundAPlayerLoadingZone = false;
                        for (Player player : Bukkit.getOnlinePlayers()){
                            if (player.getLocation().distance(personoidNPC.getLastUpdatedLocation()) <= player.getClientViewDistance()){
                                foundAPlayerLoadingZone = true;
                            }
                        }
                        personoidNPC.locationIsLoadedByPlayer = foundAPlayerLoadingZone;
                    }
                }
            }
        };
    }
}
