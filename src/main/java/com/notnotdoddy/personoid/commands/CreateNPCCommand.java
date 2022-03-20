package com.notnotdoddy.personoid.commands;

import com.notnotdoddy.personoid.handlers.CommandHandler;
import com.notnotdoddy.personoid.handlers.NPCHandler;
import com.notnotdoddy.personoid.npc.NPC;
import com.notnotdoddy.personoid.utils.bukkit.Message;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateNPCCommand extends CommandHandler.Command {
    public CreateNPCCommand() {
        super("npc", "create", CommonRequirements.player);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        NPC npc;
        if (args.length == 0) {
            npc = NPCHandler.createNPCInstance(sender.getWorld(), "Ham and Cheese", sender);
        } else if (args.length == 1) {
            npc = NPCHandler.createNPCInstance(sender.getWorld(), args[0], sender);
        } else return false;
        NPCHandler.registerNPC(npc);
        npc.spawner = sender.getUniqueId();
        NPCHandler.spawnNPC(npc, sender.getLocation());
        new Message("&aCreated NPC: &e" + npc.getName().getString()).send(sender);
        return true;
    }
}
