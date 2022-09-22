package com.personoid.api.npc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

public class Skin {
    private static final Dotenv env = Dotenv.load();
    private final String texture;
    private final String signature;

    private Skin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }

    public static Skin get(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            UUID uuid = UUID.fromString(new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString());
            return get(uuid);
        } catch (IOException e) {
            throw new RuntimeException("Error ", e);
        }
    }

    public static Skin get(UUID uuid) {
        try {
            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            return new Skin(texture, signature);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting skin from uuid", e);
        }
    }

    public static Skin get(File file) {
        String fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        if (!fileType.equals("png")) throw new RuntimeException("Error while grabbing file: type must be .png");
        try {
            return get(ImageIO.read(file));
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file as image", e);
        }
    }

    public static Skin get(BufferedImage image) {
        final byte[] buffer;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            stream.flush();
            buffer = stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while converting image to byte array", e);
        }
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("https://api.mineskin.org/generate/upload?visibility=1");
        post.setEntity(EntityBuilder.create().setBinary(buffer).setContentType(ContentType.IMAGE_PNG).build());
        JSONObject json;
        try {
            json = (JSONObject) new JSONParser().parse(EntityUtils.toString(client.execute(post).getEntity()));
            client.close();
        } catch (ParseException | IOException e) {
            throw new RuntimeException("Error while parsing JSON response", e);
        }
        JSONObject texture = (JSONObject) ((JSONObject) json.get("data")).get("texture");
        String value = (String) texture.get("value");
        String signature = (String) texture.get("signature");
        return new Skin(value, signature);
    }

    public static Skin randomDefault() {
        return new Random().nextBoolean() ? steve() : alex();
    }

    public static Skin steve() {
        return new Skin(env.get("SKIN_STEVE_TEXTURE"), env.get("SKIN_STEVE_SIGNATURE"));
    }

    public static Skin alex() {
        return new Skin(env.get("SKIN_ALEX_TEXTURE"), env.get("SKIN_ALEX_SIGNATURE"));
    }
}
