package com.personoid.npc.ai.messaging;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.personoid.PersonoidAPI;
import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCComponent;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageManager extends NPCComponent {
    private final List<String> prompts = new ArrayList<>();
    private final String name;

    public MessageManager(NPC npc) {
        super(npc);
        name = npc.getName().getString();
        prompts.addAll(List.of(
                "The following is a conversation with multiple people.",
                "Person: Hello! What are you doing here?",
                name + ": Oh, I just got bored, so I decided to play some Minecraft."
        ));
    }

    public String getResponseFrom(String input, String playerName) {
        prompts.add("\n" + playerName + ": " + input);
        prompts.add("\n" + name + ": ");
        return getResponse(List.of(playerName + ":")).trim();
    }

    public String substring(String string, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > string.length()) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        return ((beginIndex == 0) && (endIndex == string.length())) ? string : string.substring(beginIndex, subLen);
    }

    public String getResponse(String input) {
        prompts.add("\n" + input);
        prompts.add("\n" + name + ": ");
        return getResponse(new ArrayList<>());
    }

    private String getResponse(List<String> details) {
        try {
            StringEntity entity = new StringEntity(getResponseParams(details), ContentType.APPLICATION_JSON);
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost("https://api.ai21.com/studio/v1/j1-jumbo/complete");
            request.setHeaders(new Header[] { new BasicHeader("Authorization", "Bearer " + getToken()) });
            request.setEntity(entity);
            String result = new String(client.execute(request).getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(result).getAsJsonObject();
            JsonArray completions = json.getAsJsonArray("completions");
            JsonObject data = completions.get(0).getAsJsonObject().get("data").getAsJsonObject();
            String response = data.get("text").getAsString().trim();
            if (response.startsWith("(to ") && response.contains(")")) {
                response = response.substring(response.indexOf(")") + 1).trim();
            }
            prompts.set(prompts.size() - 1, prompts.get(prompts.size() - 1) + response);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getToken() {
        InputStream inputStream = PersonoidAPI.getPlugin().getClass().getResourceAsStream("/config.json");
        if (inputStream != null) {
            JSONParser parser = new JSONParser();
            try {
                JSONObject json = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                return json.get("token").toString();
            } catch (IOException | ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("File not found: config.json");
        }
    }

    private String getResponseParams(List<String> details) {
        StringBuilder prompt = new StringBuilder();
        prompts.forEach(prompt::append);
        List<String> stopSequences = new ArrayList<>(List.of("\n"));
        if (details != null && details.size() > 0) {
            stopSequences.add(details.get(0));
        }
        return new JSONObject(Map.of(
                "prompt", prompt.toString(),
                "numResults", 1,
                "maxTokens", 60,
                "temperature", 0.2,
                "topKReturn", 0,
                "topP", 1,
                "countPenalty", new JSONObject(Map.of(
                        "scale", 0.25,
                        "applyToNumbers", false,
                        "applyToPunctuations", false,
                        "applyToStopwords", false,
                        "applyToWhitespaces", false,
                        "applyToEmojis", false
                )),
                "frequencyPenalty", new JSONObject(Map.of(
                        "scale", 250,
                        "applyToNumbers", false,
                        "applyToPunctuations", false,
                        "applyToStopwords", false,
                        "applyToWhitespaces", false,
                        "applyToEmojis", false
                )),
                "presencePenalty", new JSONObject(Map.of(
                        "scale", 1,
                        "applyToNumbers", false,
                        "applyToPunctuations", false,
                        "applyToStopwords", false,
                        "applyToWhitespaces", false,
                        "applyToEmojis", false
                )),
                "stopSequences", stopSequences)
        ).toJSONString();
    }
}
