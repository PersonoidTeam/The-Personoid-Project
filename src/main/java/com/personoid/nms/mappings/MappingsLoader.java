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
    private final Map<String, String> classMappings = new LinkedHashMap<>();
    private final Map<String, NMSClass> classes = new LinkedHashMap<>();

    public MappingsLoader(MinecraftVersion version) {
        this.version = version;
    }

    private void createClassMappings(File spigotClasses, File mojangMappings) {
        try {
            Map<String, String> spigotClassMappings = new HashMap<>();

            String line;
            BufferedReader reader = new BufferedReader(new FileReader(spigotClasses));

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue; // skip comments
                String[] details = line.split(" "); // extract details
                if (details.length == 2) {
                    // spigot class mapping found
                    spigotClassMappings.put(details[0].replace("/", "."),
                            details[1].replace("/", "."));
                }
            }

            reader = new BufferedReader(new FileReader(mojangMappings));

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue; // skip comments

                // extract details
                String[] details = Arrays.stream(line.replace("-> ", "").
                        replace(":", "").split(" "))
                        .map(String::trim).toArray(String[]::new);

                if (details.length == 2) {
                    String classNoDollar = details[1].contains("$") ? details[1].substring(0, details[1].indexOf("$")) : details[1];
                    String classDollar = details[1].contains("$") ? details[1].substring(details[1].indexOf("$")) : details[1];

                    String spigotClass = spigotClassMappings.get(details[1]);
                    String spigotClassOriginal = spigotClassMappings.get(details[0]);
                    String spigotClassNoDollar = spigotClassMappings.get(classNoDollar);

                    if (spigotClass != null) { // classes that have a spigot mapping
                        classMappings.put(details[0], spigotClass);
                    } else if (spigotClassOriginal != null) { // classes that have original as obfuscated name
                        classMappings.put(details[0], spigotClassOriginal);
                    } else if (spigotClassNoDollar != null) { // scenarios where only the base class is in mappings
                        classMappings.put(details[0], spigotClassNoDollar + classDollar);
                    } else if (details[0].contains("$")) { // inner classes that do not have a spigot mapping
                        String className = details[0].substring(0, details[0].indexOf("$")) +
                                details[1].substring(details[1].indexOf("$"));
                        classMappings.put(details[0], className);
                    } else { // classes that do not have a spigot mapping, e.g. com.mojang classes
                        classMappings.put(details[0], details[0]);
                    }
                }
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

            String mojangClassName = null;
            String spigotClassName;
            NMSClass NMSClass = null;

            List<NMSConstructor> constructors = new ArrayList<>();
            List<NMSMethod> methods = new ArrayList<>();
            Map<String, NMSField> fields = new HashMap<>();

            String line;
            while ((line = reader.readLine()) != null) {
                // skip comments and static initialisers
                if (line.startsWith("#") || line.contains("<clinit>")) continue;

                // extract details
                String[] details = Arrays.stream(line.replace("-> ", "").split("\\s+"))
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

                if (!line.startsWith("  ")) { // populate and store class
                    if (NMSClass != null) {
                        NMSClass.setConstructors(constructors);
                        NMSClass.setMethods(methods);
                        NMSClass.setFields(fields);
                        for (NMSMethod nmsMethod : methods) {
                            if (nmsMethod.getMojangName().equals("setXRot")) {
                                Logger.get().info("Found x method in class " + mojangClassName);
                            }
                        }
                        classes.put(mojangClassName, NMSClass);
                    }
                    if (details.length > 0) { // initialise class
                        mojangClassName = details[0];
                        spigotClassName = getSpigotClassName(mojangClassName);
                        NMSClass = new NMSClass(mojangClassName, spigotClassName);
                        constructors = new ArrayList<>();
                        methods = new ArrayList<>();
                        fields = new HashMap<>();
                    }
                    continue;
                }

                line = line.trim();

                if (Character.isDigit(line.charAt(0))) {
                    String[] split = line.split(":");
                    line = split[split.length - 1];

                    // TODO: add support for arrays and lists etc. (encapsulated classes)
                    String[] args = Arrays.stream(details[1].substring(details[1].indexOf('(') + 1, details[1].indexOf(')')).split(","))
                            .map(s -> getSpigotClassName(s.replace("[]", "").trim()))
                            .filter(s -> !s.isEmpty())
                            .toArray(String[]::new);

                    if (line.contains("void <init>")) { // constructor
                        constructors.add(new NMSConstructor(NMSClass, args));
                    } else { // method
                        String methodName = details[1].substring(0, details[1].indexOf('(')).trim();
                        NMSMethod method = new NMSMethod(NMSClass, methodName, details[2], details[0], args);
                        methods.add(method);
                        if (methodName.equals("setXRot")) {
                            Logger.get().info("Found x method " + mojangClassName);
                        }
                    }
                } else { // field
                    fields.put(details[1], new NMSField(NMSClass, details[1], details[0], details[2]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create mappings!", e);
        }
        Logger.get("Personoid").info("Successfully created mappings");
    }

    public NMSClass createRawMapping(String className) {
        NMSClass clazz = new NMSClass(className, className);
        classes.put(className, clazz);
        return clazz;
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

    public String getMojangClassName(String spigotClass) {
        for (Map.Entry<String, String> entry : classMappings.entrySet()) {
            if (entry.getValue().equals(spigotClass)) {
                return entry.getKey();
            }
        }
        return spigotClass;
    }

    public NMSClass getClass(String clazz, boolean spigotClass) {
        if (spigotClass) clazz = getMojangClassName(clazz);
        return classes.get(clazz);
    }
}
