package com.personoid.api;

import com.personoid.api.npc.NPCHandler;
import com.personoid.v1_18_R2.NPCHandler_1_18_R2;
import com.personoid.v1_19_R1.NPCHandler_1_19_R1;
import org.bukkit.Bukkit;

import java.util.Objects;

public class PersonoidAPI {
    private static NPCHandler npcHandler;
    private static String version;

    public static NPCHandler getNPCHandler() {
        if (npcHandler != null) return npcHandler;
        return npcHandler = switch (Objects.requireNonNull(getVersion())) {
            case "v1_18_R2" -> new NPCHandler_1_18_R2();
            case "v1_19_R1" -> new NPCHandler_1_19_R1();
            default -> null;
        };
    }

    private static String getVersion() {
        if (version != null) return version;
        try {
            return version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
