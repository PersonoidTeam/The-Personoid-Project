package com.personoid.nms.mappings;

import com.personoid.api.utils.bukkit.Logger;
import com.personoid.nms.MinecraftVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MappingsDownloader {
    private static final String MAPPINGS_ROOT = "https://maven.elmakers.com/repository/org/spigotmc/minecraft-server/";
    private static final String MOJANG_MAPPINGS = MAPPINGS_ROOT + "%s-R0.1-SNAPSHOT/minecraft-server-%s-R0.1-SNAPSHOT-maps-mojang.txt";
    private static final String SPIGOT_CLASSES = MAPPINGS_ROOT + "%s-R0.1-SNAPSHOT/minecraft-server-%s-R0.1-SNAPSHOT-maps-spigot.csrg";

    private final MinecraftVersion version;

    public MappingsDownloader(MinecraftVersion version) {
        this.version = version;
    }

    public void downloadMappings() {
        Logger.get("Personoid").info("Checking for spigot class mappings...");
        if (!hasMappingsFile("spigot_classes", ".mapping")) {
            Logger.get("Personoid").info("Spigot class mappings not found, downloading...");

            String classes = String.format(SPIGOT_CLASSES, version.getDotName(), version.getDotName());
            downloadMappingFile("spigot_classes", classes);

            Logger.get("Personoid").info("Downloaded spigot class mappings");
        } else {
            Logger.get("Personoid").info("Spigot class mappings found, skipping download...");
        }

        Logger.get("Personoid").info("Checking for mojang mappings...");
        if (!hasMappingsFile("mojang", ".mapping")) {
            Logger.get("Personoid").info("Mojang mappings not found, downloading...");

            String mappings = String.format(MOJANG_MAPPINGS, version.getDotName(), version.getDotName());
            downloadMappingFile("mojang", mappings);

            Logger.get("Personoid").info("Downloaded mojang mappings");
        } else {
            Logger.get("Personoid").info("Mojang mappings found, skipping download...");
        }
    }

    private void downloadMappingFile(String mappings, String url) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String tempDir = System.getProperty("java.io.tmpdir");
                Path path = Paths.get(tempDir, "personoid", "mappings", version.getName());
                if (!Files.exists(path)) Files.createDirectories(path);

                Logger.get("Personoid").info("Writing " + mappings + " mappings to file...");
                Path mappingsFile = Files.createTempFile(path, mappings + "_", ".mapping");
                try (InputStream inputStream = response.getEntity().getContent();
                     OutputStream outputStream = Files.newOutputStream(mappingsFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                Logger.get("Personoid").info("Saved " + mappings + " mappings to file");
            } else if (response.getStatusLine().getStatusCode() == 404) {
                throw new RuntimeException(StringUtils.capitalize(mappings) + "mappings not found for " + version.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not download " + mappings + " mappings for " + version.getName(), e);
        }
    }

    public boolean hasMappingsFile(String prefix, String postfix) {
        return getMappingsFile(prefix, postfix) != null;
    }

    public File getMappingsFile(String prefix, String postfix) {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir, "personoid", "mappings", version.getName());
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
                for (Path file : dirStream) {
                    String fileName = file.getFileName().toString();
                    if (Files.isRegularFile(file) && fileName.startsWith(prefix) && fileName.endsWith(postfix)) {
                        return file.toFile();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
