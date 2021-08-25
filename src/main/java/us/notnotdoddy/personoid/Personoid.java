package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.*;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Personoid extends JavaPlugin {
    //public static String colour = "#FF00AA";

    @Override
    public void onEnable() {
        FluidPlugin.register(this, "Personoid", "&b[Personoid] ");
        initCmds();
        initListeners();
        ChatMessage.init();
        NPCHandler.startNPCTicking();
        new FluidMessage("&aPlugin enabled!").usePrefix().send();
    }

    @Override
    public void onDisable() {
        for (NPC npc : NPCHandler.getNPCs().values()) {
            npc.remove();
        }
        new FluidMessage("&cPlugin disabled!").usePrefix().send();
    }

    private void initCmds() {
        new FluidCommand("create") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (sender instanceof Player player) {
                    String name = NPCHandler.getRandomName();
                    NPCHandler.create(name, player.getLocation());
                    new FluidMessage("&aCreated new npc: &6" + name, player).usePrefix().send();
                }
                return true;
            }
        };
        new FluidCommand("remove") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (sender instanceof Player player) {
                    if (NPCHandler.getNPCs().size() > 0) {
                        NPC npc = NPCHandler.getNPCs().values().stream().toList().get(0).remove();
                        new FluidMessage("&aRemoved npc: &6" + npc.entity.getName(), player).usePrefix().send();
                    } else {
                        new FluidMessage("&cThere are no npcs to remove!", player).usePrefix().send();
                    }
                }
                return true;
            }
        };
    }

    private void initListeners() {
        new FluidListener<>(NPCDeathEvent.class) {
            @Override
            public void run() {
                getData().getNPC().spawn(getData().getNPC().getStoredLocation().getWorld().getSpawnLocation());
                NPC npc = NPCHandler.getNPCs().get(getData().getNPC());
                if (npc.damagedByPlayer != null) {
                    PlayerInfo info = npc.players.get(npc.damagedByPlayer);
                    npc.players.get(npc.damagedByPlayer).killedBy++;
                    info.mood = info.getNextMood(Behavior.MoodChangeType.ANGRY);
                    setDelay(FluidUtils.random(20, 50));
                }
            }
        }.setDelay(FluidUtils.random(20, 50));

        new FluidListener<>(NPCDamageEvent.class) {
            @Override
            public void run() {
                NPC npc = NPCHandler.getNPCs().get(getData().getNPC());
                npc.damagedByPlayer = null;
            }
        };

        new FluidListener<>(NPCDamageByEntityEvent.class) {
            @Override
            public void run() {
                if (getData().getDamager() instanceof Player player) {
                    NPC npc = NPCHandler.getNPCs().get(getData().getNPC());
                    npc.damagedByPlayer = player;
                }
            }
        };
    }
}
