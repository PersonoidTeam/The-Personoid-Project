package us.notnotdoddy.personoid.npc;

import me.definedoddy.fluidapi.FluidListener;
import me.definedoddy.fluidapi.FluidUtils;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
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
                npc.citizen.spawn(npc.spawnLocation);
                if (npc.damagedByPlayer != null) {
                    PlayerInfo info = npc.players.get(npc.damagedByPlayer);
                    npc.players.get(npc.damagedByPlayer).killedBy++;
                    info.mood = info.getNextMood(Behavior.MoodChangeType.ANGRY);
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
                    npc.damagedByPlayer = player;
                }
            }
        };
    }
}
