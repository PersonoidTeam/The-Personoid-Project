package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.*;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.notnotdoddy.personoid.npc.Behavior;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.PersonoidNPCHandler;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.PlayerInfo;

public final class Personoid extends JavaPlugin {
    //public static String colour = "#FF00AA";

    @Override
    public void onEnable() {
        FluidPlugin.register(this, "Personoid", "&b[Personoid] ");
        initCmds();
        initListeners();
        ChatMessage.init();
        new FluidMessage("&aPlugin enabled!").usePrefix().send();
    }

    @Override
    public void onDisable() {
        for (PersonoidNPC npc : PersonoidNPCHandler.getNPCs().values()) {
            npc.remove();
        }
        new FluidMessage("&cPlugin disabled!").usePrefix().send();
    }

    private void initCmds() {
        new FluidCommand("create") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (sender instanceof Player player) {
                    String name = PersonoidNPCHandler.getRandomName();
                    PersonoidNPC personoidNPC = PersonoidNPCHandler.create(name, player.getLocation());
                    personoidNPC.startNPCTicking();
                    new FluidMessage("&aCreated new npc: &6" + name, player).usePrefix().send();
                }
                return true;
            }
        };
        new FluidCommand("remove") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (sender instanceof Player player) {
                    if (PersonoidNPCHandler.getNPCs().size() > 0) {
                        PersonoidNPC npc = PersonoidNPCHandler.getNPCs().values().stream().toList().get(0).remove();
                        new FluidMessage("&aRemoved npc: &6" + npc.citizen.getName(), player).usePrefix().send();
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
                PersonoidNPC npc = PersonoidNPCHandler.getNPCs().get(getData().getNPC());
                npc.citizen.spawn(npc.spawnLocation);
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
                PersonoidNPC npc = PersonoidNPCHandler.getNPCs().get(getData().getNPC());
                npc.damagedByPlayer = null;
            }
        };

        new FluidListener<>(NPCDamageByEntityEvent.class) {
            @Override
            public void run() {
                if (getData().getDamager() instanceof Player player) {
                    PersonoidNPC npc = PersonoidNPCHandler.getNPCs().get(getData().getNPC());
                    npc.damagedByPlayer = player;
                }
            }
        };
    }
}
