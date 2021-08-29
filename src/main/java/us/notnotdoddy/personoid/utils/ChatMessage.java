package us.notnotdoddy.personoid.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.definedoddy.fluidapi.FluidListener;
import me.definedoddy.fluidapi.FluidMessage;
import me.definedoddy.fluidapi.FluidPlugin;
import me.definedoddy.fluidapi.FluidUtils;
import me.definedoddy.fluidapi.tasks.DelayedTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;
import us.notnotdoddy.personoid.status.Behavior;
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.PersonoidNPCHandler;

import java.io.*;

public class ChatMessage {
    private static JsonObject intents;

    public ChatMessage(String message, PersonoidNPC npc) {
        new FluidMessage("<" + npc.citizen.getName() + "> " + message, FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
    }

    public static void init() {
        InputStream stream = FluidPlugin.getPlugin().getClass().getResourceAsStream("/intents.json");
        File file = new File("intents.json");
        try {
            FileUtils.copyInputStreamToFile(stream, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            intents = (JsonObject) new JsonParser().parse(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initListeners();
    }

    private static void initListeners() {
        new FluidListener<>(AsyncPlayerChatEvent.class) {
            @Override
            public void run() {
                PersonoidNPC npc = null;
                for (PersonoidNPC potential : PersonoidNPCHandler.getNPCs().values()) {
                    if (getData().getMessage().toLowerCase().contains(potential.citizen.getName().toLowerCase())) {
                        npc = potential;
                    }
                }
                if (npc == null) {
                    npc = PersonoidNPCHandler.getNPCs().values().stream().toList().get(FluidUtils.random(0, PersonoidNPCHandler.getNPCs().size() - 1));
                }
                if (FluidUtils.random(1, 10) >= 3) {
                    PersonoidNPC finalNpc = npc;
                    new DelayedTask(FluidUtils.random(10, 60)) {
                        @Override
                        public void run() {
                            finalNpc.pause();
                            String response = getResponse(getData().getPlayer(), getData().getMessage(), finalNpc);
                            int delay = response == null ? FluidUtils.random(0, 10) :
                                    Math.min(Math.max(response.length() * 5 + FluidUtils.random(0, 10), 5), 100);
                            new DelayedTask(delay) {
                                @Override
                                public void run() {
                                    if (response != null) {
                                        new ChatMessage(response, finalNpc);
                                    }
                                    finalNpc.resume();
                                }
                            };
                        }
                    };
                }
            }
        };
    }

    private static String getResponse(Player player, String message, PersonoidNPC npc) {
        Behavior.Mood mood = npc.players.get(player).mood;
        JsonArray array = (JsonArray) intents.get("intents");
        JsonObject closestIntent = null;
        int closestDistance = Integer.MAX_VALUE;
        for (JsonElement intent : array) {
            JsonArray patterns = ((JsonObject)intent).get("patterns").getAsJsonArray();
            for (JsonElement pattern : patterns) {
                String string = pattern.toString();
                string = string.substring(1, string.length() - 1);
                int distance = StringUtils.getLevenshteinDistance(message, string);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIntent = intent.getAsJsonObject();
                }
            }
        }
        if (closestDistance < 15) {
            JsonObject responses = (JsonObject)(closestIntent).get("responses");
            return getRandomFromMood(mood, responses);
        }
        JsonObject responses = (JsonObject) intents.get("backup");
        return getRandomFromMood(mood, responses);
    }

    @Nullable
    private static String getRandomFromMood(Behavior.Mood mood, JsonObject responses) {
        JsonElement list = responses.get(mood.name().toLowerCase());
        JsonElement element = ((JsonArray)list).get(FluidUtils.random(0, responses.size() - 1));
        if (element != null) {
            String response = element.toString();
            return response.substring(1, response.length() - 1);
        } else {
            return null;
        }
    }
}
