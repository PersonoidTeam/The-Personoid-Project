package com.personoid.commands;

import com.personoid.handlers.CommandHandler.Command.Requirement;
import com.personoid.handlers.CommandHandler.Command.Requirement.Type;
import com.personoid.utils.bukkit.Message;

public class CommonRequirements {
    public static final Requirement player = new Requirement(Type.PLAYER) {
        @Override
        public String onFailure() {
            return Message.toColor("&cYou must be a player to use this command!");
        }
    };
}
