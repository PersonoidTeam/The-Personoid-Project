package com.notnotdoddy.personoid.commands;

import com.notnotdoddy.personoid.handlers.CommandHandler;
import com.notnotdoddy.personoid.handlers.NPCHandler;
import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.utils.bukkit.Message;
import com.notnotdoddy.personoid.utils.LocationUtils;
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
        } else if (args.length == 1) {
            npc = NPCHandler.getNPC(args[0]);
            if (npc == null) {
                new Message("&cNPC not found").send(sender);
                return true;
            }
        } else return false;
        NPCHandler.unregisterNPC(npc);
        NPCHandler.despawnNPC(npc);
        new Message("&aRemoved NPC: &e" + npc.getName().getString()).send(sender);
        return true;
    }
}
