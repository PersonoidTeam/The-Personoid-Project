package us.notnotdoddy.personoid;

import me.definedoddy.fluidapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import us.notnotdoddy.personoid.npc.NPCHandler;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.utils.DebugMessage;
import us.notnotdoddy.personoid.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {
    @CommandHook("create")
    public void createCmd(CommandInfo info) {
        if (info.getSender() instanceof Player player) {
            String name = NPCHandler.getRandomName();
            PersonoidNPC personoidNPC = NPCHandler.create(name, player.getLocation());
            personoidNPC.startTicking();
            new FluidMessage("&aCreated new npc: &6" + name,
                    FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).usePrefix().send();
        }
    }

    @CommandHook("remove")
    public void removeCmd(CommandInfo info) {
        if (info.getSender() instanceof Player) {
            if (NPCHandler.getNPCs().size() > 0) {
                PersonoidNPC npc = NPCHandler.getNPCs().values().stream().toList().get(0).remove();
                new FluidMessage("&aRemoved npc: &6" + npc.citizen.getName(),
                        FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).usePrefix().send();
            } else {
                new FluidMessage("&cThere are no npcs to remove!",
                        FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).usePrefix().send();
            }
        }
    }

    @CommandHook("toggledebug")
    public void toggleDebugCmd(CommandInfo info) {
        String[] args = info.getArgs();
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
    }

    @CommandTabHook("toggledebug")
    public List<CommandArgument> toggleDebugTab() {
        return CommandArgument.from(List.of("all", "default", "other", "food", "resource", "goal"));
    }

    @CommandHook("debugconsole")
    public void debugConsoleCmd() {
        DebugMessage.toggleConsole();
        new FluidMessage(DebugMessage.console() ? "Enabled debug for console" : "Disabled debug for console",
                FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
    }

    @CommandHook("craft")
    public boolean craftCmd(CommandInfo info) {
        if (info.getSender() instanceof Player player) {
            String[] args = info.getArgs();
            if (args.length == 1) {
                if (Arrays.stream(Material.values()).toList().toString().contains(args[0].toUpperCase())) {
                    Material material = Material.valueOf(args[0].toUpperCase());
                    PersonoidNPC npc = LocationUtils.getClosestNPC(player.getLocation());
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
            }
        }
        return false;
    }

    @CommandTabHook("craft")
    public List<CommandArgument> craftTab() {
        List<CommandArgument> args = new ArrayList<>();
        for (Material material : Material.values()) {
            args.add(new CommandArgument(material.toString().toLowerCase(), 1));
        }
        return args;
    }
}
