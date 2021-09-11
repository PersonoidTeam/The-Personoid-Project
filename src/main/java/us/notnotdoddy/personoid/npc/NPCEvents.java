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
import us.notnotdoddy.personoid.player.PlayerInfo;

public class NPCEvents {
    public static void init() {
        new FluidListener<>(NPCDeathEvent.class) {
            @Override
            public void run() {
                PersonoidNPC npc = NPCHandler.getNPCs().get(getData().getNPC());
                npc.initialised = false;
                npc.spawn(npc.data.spawnPoint);
                npc.reset();
                if (npc.data.lastDamager != null) {
                    PlayerInfo info = npc.data.players.get(npc.data.lastDamager);
                    npc.data.players.get(npc.data.lastDamager).killedBy++;
                    info.incrementMoodStrength(Behavior.Mood.ANGRY, 0.25F);
                }
                setDelay(FluidUtils.random(20, 50));
            }
        }.setDelay(FluidUtils.random(20, 50));

        new FluidListener.Group() {
            @EventHandler
            public void damage(NPCDamageEvent e) {
                PersonoidNPC npc = NPCHandler.getNPCs().get(e.getNPC());
                npc.data.lastDamager = null;
            }

            @EventHandler
            public void damageByEntity(NPCDamageByEntityEvent e) {
                if (e.getDamager() instanceof Player player) {
                    PersonoidNPC npc = NPCHandler.getNPCs().get(e.getNPC());
                    PlayerInfo info = npc.data.players.get(player.getUniqueId());
                    npc.data.lastDamager = player.getUniqueId();
                    info.incrementMoodStrength(Behavior.Mood.ANGRY, ((float) (1.1F * e.getDamage()))/10);
                }
            }
        };

        new RepeatingTask(0, 5, true) {
            @Override
            public void run() {
                for (PersonoidNPC personoidNPC : NPCHandler.getNPCs().values()){
                    if (personoidNPC.isHibernating()){
                        boolean foundAPlayerLoadingZone = false;
                        for (Player player : Bukkit.getOnlinePlayers()){
                            if (player.getLocation().distance(personoidNPC.getLastLocation()) <= player.getClientViewDistance()){
                                foundAPlayerLoadingZone = true;
                            }
                        }
                        personoidNPC.data.playerLoaded = foundAPlayerLoadingZone;
                    }
                }
            }
        };
    }
}
