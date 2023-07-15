package com.personoid.nms.mappings;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.personoid.api.utils.bukkit.Logger;
import com.personoid.nms.MinecraftVersion;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingsLoader {
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private final MinecraftVersion version;
    private final Map<String, MappedClass> classes = new HashMap<>();

    public MappingsLoader(MinecraftVersion version) {
        this.version = version;
    }

    public void loadMappings() {
        if (!hasMappingsFile("minecraft", ".mapping")) {
            Logger.get().severe("downloading mappings...");
            downloadMappings(this::createMappings);
        } else {
            createMappings();
        }
        String string = "java.lang.Long minSq -> f";
        Logger.get().severe(String.join(",", string.replace("-> ", "").split(" ")));
        string = "84:99:com.google.gson.JsonElement serializeToJson() -> c";
        Logger.get().severe(String.join(",", string.replace("-> ", "").split(" ")));
    }

    private void downloadMappings(Runnable callback) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(VERSION_MANIFEST_URL);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonObject json = new Gson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);
                JsonArray versions = json.getAsJsonArray("versions");
                for (JsonElement jsonVersion : versions) {
                    String id = jsonVersion.getAsJsonObject().get("id").getAsString();
                    if (id.equals(version.getDotName())) {
                        String packageUrl = jsonVersion.getAsJsonObject().get("url").getAsString();
                        String mappingsUrl = getVersionMappingsUrl(packageUrl);
                        if (mappingsUrl != null) {
                            writeMappingsToFile(mappingsUrl, callback);
                        } else {
                            throw new RuntimeException("Version mappings not found for " + this.version.getName());
                        }
                        return;
                    }
                }
            } else if (response.getStatusLine().getStatusCode() == 404) {
                throw new RuntimeException("Version package not found for " + this.version.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not get version package for " + this.version.getName(), e);
        }
    }

    private String getVersionMappingsUrl(String packageUrl) {
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

    private void writeMappingsToFile(String mappingsUrl, Runnable callback) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(mappingsUrl);
            client.execute(request, response -> {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String tempDir = System.getProperty("java.io.tmpdir");
                    Path path = Paths.get(tempDir, "personoid", "mappings", version.getName());
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    Logger.get().severe("Writing mappings to file...");
                    Path mappings = Files.createTempFile(path, "minecraft_", ".mapping");
                    try (InputStream inputStream = response.getEntity().getContent();
                         OutputStream outputStream = Files.newOutputStream(mappings)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    Logger.get().severe("Written mappings to file");
                    callback.run();
                } else if (response.getStatusLine().getStatusCode() == 404) {
                    throw new RuntimeException("Mappings not found for " + version.getName());
                }
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not download mappings for " + version.getName(), e);
        }
    }


    public void createMappings() {
        Logger.get().severe("creating mappings...");
        try (InputStream mappings = Files.newInputStream(getMappingsFile("minecraft", ".mapping").toPath())) {
            Map<String, MappedField> currentFields = new HashMap<>();
            Map<String, MappedMethod> currentMethods = new HashMap<>();
            List<MappedConstructor> currentConstructors = new ArrayList<>();
            String currentClass = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(mappings, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\n", "");
                if (line.startsWith("#")) {
                    continue;
                }

                if (!currentClass.equals("") && line.startsWith("    ")) {
                    String[] args = line.substring(4).split(" ");
                    if (line.contains("(")) {
                        String returnType = mapClassName(args[0]);
                        String function = args[1];
                        String functionName = function.substring(0, function.indexOf("("));
                        if (functionName.contains(":")) {
                            functionName = functionName.substring(function.lastIndexOf(":") + 1);
                        }
                        String obfuscatedName = args[3];

                        String[] functionArguments = function.substring(function.indexOf("(") + 1, function.indexOf(")")).split(",");
                        if (functionArguments[0].equals("")) {
                            functionArguments = new String[]{"_"};
                        }

                        if (function.contains("<clinit>")) {
                            continue;
                        }
                        if (function.contains("<init>")) {
                            classConstructors.put("a", new MappedConstructor(functionArguments));
                            continue;
                        }

                        List<MappedMethod> spigotMapping = methodSpigotMappings.get(mapClassName(currentClass));
                        boolean found = false;
                        for (MappedMethod _method : spigotMapping) {
                            if (!_method.getObfuscatedName().equals(obfuscatedName)) {
                                continue;
                            }
                            if (Arrays.equals(_method.getArguments(), functionArguments)) {
                                found = true;
                            }
                            if (!found) {
                                continue;
                            }

                            classMethods.put(functionName, new MappedMethod(obfuscatedName, returnType, functionArguments));
                        }

                        String fieldType = mapClassName(args[0]);
                        String fieldName = args[1];
                        obfuscatedName = args[3];

                        classFields.put(fieldName, new MappedField(fieldType, obfuscatedName));
                        continue;
                    }
                    currentClass = mapClassName(line.split(" ")[1]);
                }
            }
            Logger.get().severe("created mappings");
            Logger.get().severe("mappings: " + classes.size());
        } catch (IOException e) {
            throw new RuntimeException("Could not read mappings file for " + this.version.getName(), e);
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

    public Map<String, MappedClass> getClasses() {
        return classes;
    }
}
