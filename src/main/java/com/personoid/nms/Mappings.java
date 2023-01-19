package com.personoid.nms;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Mappings {
    private static final String SPIGOT_CLASSES = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/bukkit-{0}-cl.csrg?at={1}";
    private static final String SPIGOT_METHODS = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/bukkit-{0}-members.csrg?at={1}";
    private static final String MINECRAFT_MAPPINGS = "https://launcher.mojang.com/v1/objects/{0}/server.txt";

    private final MinecraftVersion version;

    public static Mappings get(MinecraftVersion version) {
        return new Mappings(version);
    }

    private Mappings(MinecraftVersion version) {
        this.version = version;
        loadMappings();
    }

    private void loadMappings() {
        fetchAndWrite(getSpigotClassesUrl(), "classes");
        fetchAndWrite(getSpigotMethodsUrl(), "methods");
        fetchAndWrite(getMinecraftMappingsUrl(), "minecraft");
    }

    private String getSpigotClassesUrl() {
        return SPIGOT_CLASSES.replace("{0}", version.getDotName()).replace("{1}", version.getSpigotCommit());
    }

    private String getSpigotMethodsUrl() {
        return SPIGOT_METHODS.replace("{0}", version.getDotName()).replace("{1}", version.getSpigotCommit());
    }

    private String getMinecraftMappingsUrl() {
        return MINECRAFT_MAPPINGS.replace("{0}", version.getMinecraftVersion());
    }

    private void fetchAndWrite(String url, String fileName) {
        try {
            try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                HttpGet request = new HttpGet(url);
                client.execute(request, response -> {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        File mappings = File.createTempFile("mappings", fileName);
                        response.getEntity().writeTo(Files.newOutputStream(mappings.toPath()));
                    }
                    return null;
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
