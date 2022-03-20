package com.notnotdoddy.personoid.commands;

import com.notnotdoddy.personoid.handlers.CommandHandler;
import com.notnotdoddy.personoid.utils.debug.Profiler;
import com.notnotdoddy.personoid.utils.bukkit.Message;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ProfilerMessageCommand extends CommandHandler.Command {
    public ProfilerMessageCommand() {
        super("profiler", "message");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, String[] args) {
        if (args.length == 1) {
            for (Profiler.Type profilerType : Profiler.Type.values()) {
                if (profilerType.name().equalsIgnoreCase(args[0].trim())) {
                    if (Profiler.isMessageEnabled(profilerType)) {
                        Profiler.disableMessage(profilerType);
                        new Message("&cDisabled &6" + profilerType.name() + "&c profiling messages").send(sender);
                    } else {
                        Profiler.enableMessage(profilerType);
                        new Message("&aEnabled &6" + profilerType.name() + "&a profiling messages").send(sender);
                    }
                    return true;
                }
            }
            new Message("&cInvalid profiler type").send(sender);
        } else return false;
        return true;
    }
}
