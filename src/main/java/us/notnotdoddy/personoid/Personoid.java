package us.notnotdoddy.personoid;

import com.google.gson.Gson;
import me.definedoddy.fluidapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.PersonoidNPCEvents;
import us.notnotdoddy.personoid.npc.PersonoidNPCHandler;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;

import java.util.Arrays;

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
        new FluidCommand("toggledebug") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                DebugMessage.enabled = !DebugMessage.enabled;
                new FluidMessage("Toggled debug to " + (DebugMessage.enabled ? "on" : "off"), sender).send();
                return true;
            }
        };
        new FluidCommand("craft") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (sender instanceof Player player) {
                    if (args.length == 1) {
                        if (Arrays.stream(Material.values()).toList().toString().contains(args[0].toUpperCase())) {
                            Material material = Material.valueOf(args[0].toUpperCase());
                            PersonoidNPC npc = LocationUtilities.getClosestNPC(player.getLocation());
                            if (npc.data.currentGoal != null){
                                npc.data.currentGoal.endGoal(npc);
                                npc.data.currentGoal = null;
                            }
                            npc.forgetCurrentTarget();
                            npc.data.resourceManager.isDoingSomething = true;
                            npc.data.resourceManager.attemptCraft(material);
                            new FluidMessage("Sent crafting instructions for &a" + material.getKey().getKey().toLowerCase() + "&r to &6" +
                                    npc.citizen.getName(), sender).send();
                        }
                    } else return false;
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
