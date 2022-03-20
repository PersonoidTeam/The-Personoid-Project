package com.notnotdoddy.personoid.commands;

import com.notnotdoddy.personoid.handlers.CommandHandler.Command.Requirement;
import com.notnotdoddy.personoid.handlers.CommandHandler.Command.Requirement.Type;
import com.notnotdoddy.personoid.utils.message.Message;

public class CommonRequirements {
    public static final Requirement player = new Requirement(Type.PLAYER) {
        @Override
        public String onFailure() {
            return Message.toColor("&cYou must be a player to use this command!");
        }
    };
}
