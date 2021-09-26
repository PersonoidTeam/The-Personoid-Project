package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidPlugin;
import me.definedoddy.fluidapi.FluidTask;
import me.definedoddy.fluidapi.utils.JavaUtils;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.notnotdoddy.personoid.player.PlayerInfo;
import us.notnotdoddy.personoid.status.Behavior;

public class NPCEvents implements Listener {
    @EventHandler
    public void npcDeath(NPCDeathEvent e) {
        new FluidTask(() -> {
            PersonoidNPC npc = NPCHandler.getNPCs().get(e.getNPC());
            npc.initialised = false;
            npc.spawn(npc.data.spawnPoint);
            npc.reset();
            if (npc.data.lastDamager != null) {
                PlayerInfo info = npc.data.players.get(npc.data.lastDamager);
                npc.data.players.get(npc.data.lastDamager).killedBy++;
                info.incrementMoodStrength(Behavior.Mood.ANGRY, 0.25F);
            }
        }).run(JavaUtils.random(20, 50));
    }

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

    public static void init() {
        Bukkit.getServer().getPluginManager().registerEvents(new NPCEvents(), FluidPlugin.getPlugin());
        new FluidTask(() -> {
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
        }).async().repeat(0, 5);
    }
}
