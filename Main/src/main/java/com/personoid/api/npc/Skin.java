package com.personoid.api.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;

public class Skin {
    private final String texture;
    private final String signature;

    private Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public static Skin get(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = JsonParser.parseReader(reader).getAsJsonObject().get("id").getAsString();
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = JsonParser.parseReader(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            return new Skin(texture, signature);
        } catch (Exception ignored) {}
        return null;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }
}
