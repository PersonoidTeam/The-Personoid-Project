package com.personoid.commands;

import com.personoid.handlers.CommandHandler;
import com.personoid.handlers.NPCHandler;
import com.personoid.npc.NPC;
import com.personoid.utils.bukkit.Message;
import com.personoid.utils.LocationUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RemoveNPCCommand extends CommandHandler.Command {
    public RemoveNPCCommand() {
        super("npc", "remove", CommonRequirements.player);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        NPC npc;
        if (args.length == 0) {
            npc = LocationUtils.getClosestNPC(sender.getLocation());
            if (npc == null) {
                new Message("&cNo NPC found").send(sender);
                return true;
            }
        } else if (args.length == 1) {
            npc = NPCHandler.getNPC(args[0]);
            if (npc == null) {
                new Message("&cNPC not found").send(sender);
                return true;
            }
        } else return false;
        NPCHandler.despawnNPC(npc);
        NPCHandler.unregisterNPC(npc);
        new Message("&aRemoved NPC: &e" + npc.getName().getString()).send(sender);
        return true;
    }
}
