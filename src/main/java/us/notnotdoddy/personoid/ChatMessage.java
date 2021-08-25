package us.notnotdoddy.personoid;

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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.*;

public class ChatMessage {
    private static JsonObject intents;

    public ChatMessage(String message, NPC npc) {
        new FluidMessage("<" + npc.entity.getName() + "> " + message, FluidMessage.toPlayerArray(Bukkit.getOnlinePlayers())).send();
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
                if (FluidUtils.random(1, 10) >= 0) { //3
                    NPC npc = NPCHandler.getNPCs().values().stream().toList().get(0);
                    new DelayedTask(FluidUtils.random(10, 60)) {
                        @Override
                        public void run() {
                            npc.pause();
                            String response = getResponse(getData().getPlayer(), getData().getMessage(), npc);
                            int delay = Math.min(Math.max(response.length() * 5 + FluidUtils.random(0, 10), 5), 100);
                            new DelayedTask(delay) {
                                @Override
                                public void run() {
                                    new ChatMessage(response, npc);
                                    npc.resume();
                                }
                            };
                        }
                    };
                }
            }
        };
    }

    private static String getResponse(Player player, String message, NPC npc) {
        Behavior.Mood mood = npc.players.get(player).mood;
        JsonArray array = (JsonArray) intents.get("intents");
        for (JsonElement intent : array) {
            JsonArray patterns = ((JsonObject)intent).get("patterns").getAsJsonArray();
            if (patterns.getAsJsonArray().toString().contains(message)) {
                JsonObject responses = (JsonObject)((JsonObject) intent).get("responses");
                JsonElement element = responses.get(mood.name().toLowerCase());
                String response = ((JsonArray)element).get(FluidUtils.random(0, responses.size() - 1)).toString();
                return response.substring(1, response.length() - 1);
            }
        }
        JsonObject responses = (JsonObject) intents.get("backup");
        JsonElement element = responses.get(mood.name().toLowerCase());
        String response = ((JsonArray)element).get(FluidUtils.random(0, responses.size() - 1)).toString();
        return response.substring(1, response.length() - 1);
    }
}
