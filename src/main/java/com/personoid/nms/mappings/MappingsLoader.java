package com.personoid.nms.mappings;

import com.personoid.api.utils.bukkit.Logger;
import com.personoid.nms.MinecraftVersion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MappingsLoader {
    private final MinecraftVersion version;
    private final Map<String, String> classMappings = new HashMap<>();
    private final Map<String, MappedClass> classes = new HashMap<>();

    public MappingsLoader(MinecraftVersion version) {
        this.version = version;
    }

    private void createClassMappings(File spigotClasses, File mojangMappings) {
        try {
            Map<String, String> spigotClassMappings = new HashMap<>();

            BufferedReader reader = new BufferedReader(new FileReader(spigotClasses));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\n", "");
                if (line.startsWith("#")) {
                    continue;
                }
                String[] info = line.split(" ");
                spigotClassMappings.put(info[0], info[1]);
            }
            reader.close();

            reader = new BufferedReader(new FileReader(mojangMappings));
            while ((line = reader.readLine()) != null) {
                line = line.replace("\n", "").replace(":", "");
                if (line.startsWith("#") || line.startsWith("    ")) {
                    continue;
                }

                String[] classDetails = line.split(" -> ");
                String className;
                if (spigotClassMappings.containsKey(classDetails[1])) {
                    className = spigotClassMappings.get(classDetails[1]);
                } else {
                    className = classDetails[0];
                }

                classMappings.put(classDetails[0], className.replace("/", "."));
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createMappings() {
        Logger.get("Personoid").info("Creating mappings...");
        createClassMappings(
                getMappingsFile("spigot_classes", ".mapping"),
                getMappingsFile("mojang", ".mapping")
        );

        InputStream mappings;
        try {
            mappings = new FileInputStream(getMappingsFile("mojang", ".mapping"));
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
            throw new RuntimeException("Failed to create mappings!", e);
        }
        Logger.get("Personoid").info("Successfully created mappings");
    }

    public File getMappingsFile(String prefix, String postfix) {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir + "/personoid/mappings/" + version.getName());
        if (!path.toFile().exists()) {
            throw new IllegalStateException("Mappings file does not exist");
        }
        File[] files = path.toFile().listFiles();
        if (files == null) {
            throw new IllegalStateException("Mappings file does not exist");
        }
        for (File file : files) {
            if (file.getName().startsWith(prefix) && file.getName().endsWith(postfix)) {
                return file;
            }
        }
        throw new IllegalStateException("Mappings file does not exist");
    }

    public String getSpigotClassName(String mojangClass) {
        if (classMappings.containsKey(mojangClass)) {
            return classMappings.get(mojangClass);
        }
        return mojangClass;
    }

    public MappedClass getClass(String mojangClass) {
        return classes.get(mojangClass);
    }
}
