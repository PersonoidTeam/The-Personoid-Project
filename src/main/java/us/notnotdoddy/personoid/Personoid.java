package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.FluidCommand;
import me.definedoddy.fluidapi.FluidListener;
import me.definedoddy.fluidapi.FluidMessage;
import me.definedoddy.fluidapi.FluidPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.PersonoidNPCEvents;
import us.notnotdoddy.personoid.npc.PersonoidNPCHandler;
import us.notnotdoddy.personoid.utils.ChatMessage;

public final class Personoid extends JavaPlugin {
    //public static String colour = "#FF00AA";

    @Override
    public void onEnable() {
        FluidPlugin.register(this, "Personoid", "&b[Personoid] ");
        initCmds();
        initListeners();
        ChatMessage.init();
        PersonoidNPCEvents.init();
        new FluidMessage("&aPlugin enabled!").usePrefix().send();
    }

    @Override
    public void onDisable() {
        //disabled
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
        new FluidListener<>(PluginDisableEvent.class) {
            @Override
            public void run() {
                for (PersonoidNPC npc : PersonoidNPCHandler.getNPCs().values()) {
                    npc.remove();
                }
                new FluidMessage("&cPlugin disabled!").usePrefix().send();
            }
        };
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
    }
}
