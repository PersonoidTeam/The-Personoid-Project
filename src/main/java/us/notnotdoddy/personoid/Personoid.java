package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.FluidCommand;
import me.definedoddy.fluidapi.FluidListener;
import me.definedoddy.fluidapi.FluidMessage;
import me.definedoddy.fluidapi.FluidPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import us.notnotdoddy.personoid.npc.NPCEvents;
import us.notnotdoddy.personoid.npc.NPCHandler;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.utils.ChatMessage;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.LocationUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Personoid extends JavaPlugin {
    //public static String colour = "#FF00AA";

    @Override
    public void onEnable() {
        FluidPlugin.register(this, "Personoid", "&b[Personoid] ");
        initCmds();
        initListeners();
        ChatMessage.init();
        NPCEvents.init();
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
                    String name = NPCHandler.getRandomName();
                    PersonoidNPC personoidNPC = NPCHandler.create(name, player.getLocation());
                    personoidNPC.startTicking();
                    new FluidMessage("&aCreated new npc: &6" + name,
                            FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).usePrefix().send();
                }
                return true;
            }
        };
        new FluidCommand("remove") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (sender instanceof Player player) {
                    if (NPCHandler.getNPCs().size() > 0) {
                        PersonoidNPC npc = NPCHandler.getNPCs().values().stream().toList().get(0).remove();
                        new FluidMessage("&aRemoved npc: &6" + npc.citizen.getName(),
                                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).usePrefix().send();
                    } else {
                        new FluidMessage("&cThere are no npcs to remove!",
                                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).usePrefix().send();
                    }
                }
                return true;
            }
        };
        FluidCommand debugCommand = new FluidCommand("toggledebug") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                if (args.length > 0 && !args[0].equals(" ")) {
                    StringBuilder key = new StringBuilder();
                    List<String> filtered = new ArrayList<>(Arrays.stream(args).toList());
                    for (int i = 0; i < filtered.size(); i++) {
                        key.append(filtered.get(i));
                        if (i < filtered.size() - 1) {
                            key.append(" ");
                        }
                    }
                    if (DebugMessage.isKeyActive(key.toString())) {
                        DebugMessage.removeKey(key.toString());
                        new FluidMessage("Disabled debug for &c" + key,
                                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                    } else {
                        DebugMessage.addKey(key.toString());
                        new FluidMessage("Enabled debug for &c" + key,
                                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                    }
                } else {
                    if (DebugMessage.isKeyActive("default")) {
                        DebugMessage.removeKey("default");
                        new FluidMessage("Disabled debug for &cdefault",
                                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                    } else {
                        DebugMessage.addKey("default");
                        new FluidMessage("Enabled debug for &cdefault",
                                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                    }
                }
                return true;
            }
        };
        for (String key : List.of("all", "default", "other", "food", "resource", "goal")) {
           debugCommand.addTabArguments(new FluidCommand.Argument(key, 1));
        }
        new FluidCommand("debugconsole") {
            @Override
            public boolean run(CommandSender sender, Command cmd, String[] args) {
                DebugMessage.toggleConsole();
                new FluidMessage(DebugMessage.console() ? "Enabled debug for console" : "Disabled debug for console",
                        FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                return true;
            }
        };
        FluidCommand craftCommand = new FluidCommand("craft") {
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
                            npc.forgetTarget();
                            npc.data.resourceManager.isPaused = false;
                            npc.data.resourceManager.isDoingSomething = true;
                            npc.data.resourceManager.attemptCraft(material);
                            new FluidMessage("Sent crafting instructions for &a" + material.getKey().getKey().toLowerCase() + "&r to &6" +
                                    npc.citizen.getName(), FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                        }
                    } else return false;
                }
                return true;
            }
        };
        for (Material material : Material.values()) {
            craftCommand.addTabArguments(new FluidCommand.Argument(material.toString().toLowerCase(), 1));
        }
    }

    private void initListeners() {
        new FluidListener<>(PluginDisableEvent.class) {
            @Override
            public void run() {
                for (PersonoidNPC npc : NPCHandler.getNPCs().values()) {
                    npc.remove();
                }
                new FluidMessage("&cPlugin disabled!").usePrefix().send();
            }
        };
        getServer().getPluginManager().registerEvents(new GameEvents(), this);
    }
}
