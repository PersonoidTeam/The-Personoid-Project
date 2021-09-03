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
import us.notnotdoddy.personoid.npc.PersonoidNPC;
import us.notnotdoddy.personoid.npc.PersonoidNPCHandler;
import us.notnotdoddy.personoid.status.Behavior;

import java.io.*;
import java.util.List;

public class ChatMessage {
    private static JsonObject intents;

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

        InputStream nameStream = FluidPlugin.getPlugin().getClass().getResourceAsStream("/names.txt");
        File nameFile = new File("names.txt");
        try {
            FileUtils.copyInputStreamToFile(nameStream, nameFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListeners();
    }

    public static void send(PersonoidNPC npc, String message) {
        if (message != null) {
            int delay = Math.min(Math.max(message.length() * 5 + FluidUtils.random(0, 10), 5), 100);
            if (delay > 0 && message.length() > 0) {
                npc.pause();
                new DelayedTask(delay) {
                    @Override
                    public void run() {
                        new FluidMessage("<" + npc.citizen.getName() + "> " + message, FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
                        npc.resume();
                    }
                };
            }
        }
    }

    public static void send(PersonoidNPC npc, String response, String message) {
        if (message.length() > 0) {
            int delay = Math.min(Math.max(message.length() * 3 + FluidUtils.random(0, 10), 5), 75);
            new DelayedTask(delay) {
                @Override
                public void run() {
                    send(npc, response);
                }
            };
        }
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
                    List<PersonoidNPC> npcs = PersonoidNPCHandler.getNPCs().values().stream().toList();
                    npc = npcs.size() > 1 ? npcs.get(FluidUtils.random(0, PersonoidNPCHandler.getNPCs().size() - 1)) : npcs.get(0);
                }
                send(npc, getResponse(getData().getPlayer(), getData().getMessage(), npc), getData().getMessage());
            }
        };
    }

    private static String getResponse(Player player, String message, PersonoidNPC npc) {
        Behavior.Mood mood = npc.players.get(player.getUniqueId()).getStrongestMood();
        JsonArray array = intents.get("intents").getAsJsonArray();
        JsonObject closestIntent = null;
        int closestDistance = Integer.MAX_VALUE;
        for (JsonElement intent : array) {
            if (intent.getAsJsonObject().has("patterns")) {
                JsonArray patterns = intent.getAsJsonObject().get("patterns").getAsJsonArray();
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
        }
        if (closestDistance < 15) {
            JsonObject responses = closestIntent.get("responses").getAsJsonObject();
            return getRandomFromMood(mood, responses);
        }
        JsonObject responses = intents.get("default").getAsJsonObject();
        return getRandomFromMood(mood, responses);
    }

    public static String getResponse(Behavior.Mood mood, String tag) {
        JsonArray array = intents.get("intents").getAsJsonArray();
        JsonObject correctIntent = null;
        for (JsonElement intent : array) {
            JsonElement element = intent.getAsJsonObject().get("tag");
            String currentTag = element.toString().substring(1, element.toString().length() - 1);
            if (currentTag.equals(tag)) {
                correctIntent = intent.getAsJsonObject();
            }
        }
        if (correctIntent != null) {
            JsonObject responses = correctIntent.get("responses").getAsJsonObject();
            return getRandomFromMood(mood, responses);
        }
        return null;
    }

    @Nullable
    private static String getRandomFromMood(Behavior.Mood mood, JsonObject responses) {
        JsonElement list = null;
        if (responses.has("all")) {
            list = responses.get("all");
        } else if (responses.has(mood.name().toLowerCase())) {
            list = responses.get(mood.name().toLowerCase());
        } else if (responses.has("default")) {
            list = responses.get("default");
        }
        if (list != null) {
            JsonElement response = list.getAsJsonArray().get(FluidUtils.random(0, list.getAsJsonArray().size() - 1));
            if (response.isJsonNull()) {
                return null;
            }
            return response.toString().substring(1, response.toString().length() - 1);
        }
        return null;
    }
}
