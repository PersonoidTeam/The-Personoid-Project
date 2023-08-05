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
import java.util.HashMap;
import java.util.Map;

public class MappingsDownloader {
    private static final String VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    //private static final String SPIGOT_COMMITS = "https://hub.spigotmc.org/versions/%s.json";
    //private static final String SPIGOT_MAPPINGS_ROOT = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/";
    //private static final String SPIGOT_CLASSES = SPIGOT_MAPPINGS_ROOT + "bukkit-%s-cl.csrg?at=%s";
    //private static final String SPIGOT_METHODS = SPIGOT_MAPPINGS_ROOT + "bukkit-%s.at?at=%s";

    private static final String MAPPINGS_ROOT = "https://maven.elmakers.com/repository/org/spigotmc/minecraft-server/";
    private static final String MOJANG_MAPPINGS = MAPPINGS_ROOT + "%s-R0.1-SNAPSHOT/minecraft-server-%s-R0.1-SNAPSHOT-maps-mojang.txt";
    private static final String SPIGOT_CLASSES = MAPPINGS_ROOT + "%s-R0.1-SNAPSHOT/minecraft-server-%s-R0.1-SNAPSHOT-maps-spigot.csrg";
    private static final String SPIGOT_METHODS = MAPPINGS_ROOT + "%s-R0.1-SNAPSHOT/minecraft-server-%s-R0.1-SNAPSHOT-maps-spigot-members.csrg";

    private final MinecraftVersion version;
    private final Map<String, MappedClass> classes = new HashMap<>();
    private String spigotBuildHash;

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

/*        Logger.get("Personoid").info("Checking for spigot method mappings...");
        if (!hasMappingsFile("spigot_methods", ".mapping")) {
            Logger.get("Personoid").info("Spigot method mappings not found, downloading...");

            String methods = String.format(SPIGOT_METHODS, version.getDotName(), version.getDotName());
            downloadMappingFile("spigot_methods", methods);

            Logger.get("Personoid").info("Downloaded spigot method mappings");
        } else {
            Logger.get("Personoid").info("Spigot method mappings found, skipping download...");
        }*/

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

/*    private String getMojangMappings(String packageUrl) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(packageUrl);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject json = new Gson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                JsonObject downloads = json.getAsJsonObject("downloads");
                if (downloads != null && downloads.has("server_mappings")) {
                    JsonObject serverMappings = downloads.getAsJsonObject("server_mappings");
                    if (serverMappings.has("url")) {
                        return serverMappings.get("url").getAsString();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not get version mappings URL for " + this.version.getName(), e);
        }
        return null;
    }

    private String getLauncherData() {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(VERSION_MANIFEST);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject json = new Gson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                JsonArray versions = json.getAsJsonArray("versions");
                for (JsonElement jsonVersion : versions) {
                    String id = jsonVersion.getAsJsonObject().get("id").getAsString();
                    if (id.equals(version.getDotName())) {
                        return jsonVersion.getAsJsonObject().get("url").getAsString();
                    }
                }
            } else if (response.getStatusLine().getStatusCode() == 404) {
                throw new RuntimeException("Version package not found for " + this.version.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not get version package for " + this.version.getName(), e);
        }
        return null;
    }*/

/*    private String getSpigotBuildHash() {
        if (spigotBuildHash != null) return spigotBuildHash;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(String.format(SPIGOT_COMMITS, version.getDotName()));
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject json = new Gson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                JsonObject refs = json.get("refs").getAsJsonObject();
                return spigotBuildHash = refs.get("BuildData").getAsString();
            } else if (response.getStatusLine().getStatusCode() == 404) {
                throw new RuntimeException("Version package not found for " + this.version.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not get version package for " + this.version.getName(), e);
        }
        return null;
    }*/

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

/*    public void createMappings() {
        InputStream mappings;
        try {
            mappings = new FileInputStream(getMappingsFile("minecraft", ".mapping"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not create input stream from .mapping file", e);
        }

        try (InputStreamReader streamReader = new InputStreamReader(mappings, StandardCharsets.UTF_8)) {
            BufferedReader reader = new BufferedReader(streamReader);

            Map<String, MappedField> currentFields = new HashMap<>();
            Map<String, MappedMethod> currentMethods = new HashMap<>();
            List<MappedConstructor> currentConstructors = new ArrayList<>();
            String currentClass = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.contains("<clinit>")) {
                    continue;
                }

                // class
                String[] details = Arrays.stream(line.replace("-> ", "")
                        .split(" ")).map(String::trim).toArray(String[]::new);
                if (!line.startsWith("  ")) {
                    if (currentClass != null) {
                        classes.put(currentClass, new MappedClass(currentMethods, currentFields, currentConstructors));
                        currentFields = new HashMap<>();
                        currentMethods = new HashMap<>();
                        currentConstructors = new ArrayList<>();
                    }
                    if (details.length > 0) {
                        currentClass = details[0];
                        if (currentClass.contains("Living")) {
                            Logger.get().severe("CLASS_: " + currentClass.replace(" ", "*"));
                        }
                    }
                    continue;
                }

                line = line.trim();

                if (Character.isDigit(line.charAt(0))) {
                    String[] split = line.split(":");
                    line = split[split.length - 1];
                    details = Arrays.stream(line.replace("-> ", "")
                            .split(" ")).map(String::trim).toArray(String[]::new);

                    String[] args = Arrays.stream(details[1].substring(details[1].indexOf('(') + 1, details[1].indexOf(')'))
                            .split(",")).filter(arg -> !arg.isEmpty()).toArray(String[]::new);
                    if (line.contains("void <init>")) {
                        // constructor
                        currentConstructors.add(new MappedConstructor(args));
                    } else {
                        // method
                        if (currentClass.contains("Living")) {
                            for (String arg : args) {
                                Logger.get().severe("ARGS: " + arg);
                            }
                        }
                        String methodName = details[1].substring(0, details[1].indexOf('('));
                        currentMethods.put(methodName, new MappedMethod(details[2], details[0], args));
                    }
                    continue;
                }

                // field
                if (details.length == 3) {
                    currentFields.put(details[1], new MappedField(details[0], details[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

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

    public Map<String, MappedClass> getClasses() {
        return classes;
    }
}
