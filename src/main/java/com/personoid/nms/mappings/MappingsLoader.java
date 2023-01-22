package com.personoid.nms.mappings;

import com.personoid.nms.MinecraftVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MappingsLoader {
    private static final String SPIGOT_CLASSES = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/bukkit-{0}-cl.csrg?at={1}";
    private static final String SPIGOT_METHODS = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/raw/mappings/bukkit-{0}-members.csrg?at={1}";
    private static final String MINECRAFT_MAPPINGS = "https://launcher.mojang.com/v1/objects/{0}/server.txt";

    private final MinecraftVersion version;
    private final Map<String, MappedClass> classes = new HashMap<>();

    public MappingsLoader(MinecraftVersion version) {
        this.version = version;
    }

    public void loadMappings() {
        if (!Mapper.getMappingsFile("classes", ".mapping", version).exists()) {
            fetchAndWrite(getSpigotClassesUrl(), "classes");
        }
        if (!Mapper.getMappingsFile("methods", ".mapping", version).exists()) {
            fetchAndWrite(getSpigotMethodsUrl(), "methods");
        }
        if (!Mapper.getMappingsFile("minecraft", ".mapping", version).exists()) {
            fetchAndWrite(getMinecraftMappingsUrl(), "minecraft");
        }
        populateMappings();
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
                        String tempDir = System.getProperty("java.io.tmpdir");
                        Path path = Paths.get(tempDir + "/mojang mappings/" + version.getName());
                        if (!path.toFile().exists()) {
                            Files.createDirectories(path);
                        }
                        Path mappings = Files.createTempFile(path, fileName + "_", ".mapping");
                        response.getEntity().writeTo(Files.newOutputStream(mappings));
                        populateMappings();
                    } else if (response.getStatusLine().getStatusCode() == 404) {
                        throw new RuntimeException("Mappings not found for " + version.getName());
                    }
                    return null;
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch mappings (" + fileName + ")", e);
        }
    }

    public void populateMappings() {
        Map<String, MappedField> currentFields = new HashMap<>();
        Map<String, MappedMethod> currentMethods = new HashMap<>();
        List<MappedConstructor> currentConstructors = new ArrayList<>();
        String currentClass = null;
        InputStream mappings;
        try {
            mappings = new FileInputStream(Mapper.getMappingsFile("mappings", ".mojmap", version));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not create input stream from .mojmap file", e);
        }
        try (InputStreamReader streamReader = new InputStreamReader(mappings, StandardCharsets.UTF_8)) {
            BufferedReader reader = new BufferedReader(streamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("  ")) {
                    if (currentClass != null) {
                        classes.put(currentClass, new MappedClass(currentMethods, currentFields, currentConstructors));
                        currentFields = new HashMap<>();
                        currentMethods = new HashMap<>();
                        currentConstructors = new ArrayList<>();
                    }
                    currentClass = line;
                    continue;
                }

                line = line.substring(2);
                String[] details = line.split(" ");

                // field
                if (details.length == 3) {
                    currentFields.put(details[1], new MappedField(details[0], details[2]));
                    continue;
                }

                // method
                if (details.length == 4) {
                    currentMethods.put(details[1], new MappedMethod(details[3], details[0],
                            Objects.equals(details[2], "_") ? new String[]{} : details[2].split(",")));
                    continue;
                }

                // constructor
                if (details.length == 2 && Objects.equals(details[0], "_")) {
                    currentConstructors.add(new MappedConstructor(details[1].split(",")));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, MappedClass> getClasses() {
        return classes;
    }
}
