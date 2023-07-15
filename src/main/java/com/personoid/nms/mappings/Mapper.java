package com.personoid.nms.mappings;

import com.personoid.nms.MinecraftVersion;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Mapper {
    private final MinecraftVersion version;

    private String currentClass = "";
    private final HashMap<String, MappedField> classFields = new HashMap<>();
    private final HashMap<String, MappedMethod> classMethods = new HashMap<>();
    private final HashMap<String, MappedConstructor> classConstructors = new HashMap<>();

    private final Map<String, String> classSpigotMappings = new HashMap<>();
    private final Map<String, List<MappedMethod>> methodSpigotMappings = new HashMap<>();
    private final Map<String, String> classMCToSpigot = new HashMap<>();

    public Mapper(MinecraftVersion version) {
        this.version = version;
    }

    private String mapClassName1(String className) {
        if (classMCToSpigot.containsKey(className)) {
            return classMCToSpigot.get(className).replace("/", ".");
        }
        return "_";
    }

    private List<String> parseArguments(String arguments) {
        List<String> args = new ArrayList<>();
        boolean readingClass = false;
        boolean array = false;
        StringBuilder clazz = new StringBuilder();
        for (char character : arguments.toCharArray()) {
            String type;
            if (readingClass) {
                if (character == ';') {
                    args.add(clazz.toString().replace("/", "."));
                    readingClass = false;
                    clazz = new StringBuilder();
                    continue;
                }
                clazz.append(character);
                continue;
            } else if (character == 'D') {
                type = "double";
            } else if (character == 'F') {
                type = "float";
            } else if (character == 'I') {
                type = "int";
            } else if (character == 'J') {
                type = "long";
            } else if (character == 'S') {
                type = "short";
            } else if (character == 'Z') {
                type = "boolean";
            } else if (character == 'B') {
                type = "byte";
            } else if (character == 'C') {
                type = "char";
            } else if (character == 'V') {
                type = "void";
            } else if (character == '[') {
                array = true;
                continue;
            } else if (character != 'L') {
                throw new IllegalArgumentException("Unknown character found while mapping: " + character);
            } else {
                readingClass = true;
                continue;
            }
            if (array) {
                array = false;
                args.add(type + "[]");
            }
        }
        return args;
    }

    public String mapClassName(String oldClassName) {
        String newClassName = mapClassName1(oldClassName);
        if (newClassName.equals("_")) {
            newClassName = oldClassName;
        }
        if (newClassName.contains(":")) {
            newClassName = newClassName.split(":")[1];
        }
        if (!newClassName.contains(".") && !newClassName.equals("float") && !newClassName.equals("byte") && !newClassName.equals("int") &&
                !newClassName.equals("double") && !newClassName.equals("long") && !newClassName.equals("short") && !newClassName.equals("char")) {
            return "net.minecraft.server." + newClassName;
        }
        return newClassName;
    }

    public void initialiseSpigot(String bukkit, String bukkitMethods) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(bukkit));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replace("\n", "");
            if (line.startsWith("#")) {
                continue;
            }
            String[] info = line.split(" ");
            classSpigotMappings.put(info[0], info[1]);
        }
        reader.close();

        reader = new BufferedReader(new FileReader(bukkitMethods));
        while ((line = reader.readLine()) != null) {
            line = line.replace("\n", "");
            if (line.startsWith("#")) {
                continue;
            }
            String[] info = line.split(" ");
            String className = info[0].replace("/", ".");

            if (!line.contains("(")) {
                continue;
            }
            List<String> args = parseArguments(info[2].substring(1, info[2].indexOf(")")));

            List<MappedMethod> array;
            if (methodSpigotMappings.containsKey(className)) {
                array = methodSpigotMappings.get(className);
            } else {
                array = new ArrayList<>();
            }
            array.add(new MappedMethod(info[1], info[3], args.toArray(new String[0])));
            methodSpigotMappings.put(className, array);
        }
        reader.close();
    }

    public void initialiseMC(String mojang) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(mojang));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replace("\n", "").replace(":", "");
            if (line.startsWith("#") || line.startsWith("    ")) {
                continue;
            }

            String[] classDetails = line.split(" -> ");
            String className;
            if (classSpigotMappings.containsKey(classDetails[1])) {
                className = classSpigotMappings.get(classDetails[1]);
            } else {
                className = classDetails[0];
            }

            classMCToSpigot.put(classDetails[0], className.replace("/", "."));
        }
        reader.close();
    }

    public void map() {
        try {
            String mcVersion = ""; //version.getMinecraftVersion();
            String bukkit = String.format("sources/%s-class.s_", mcVersion);
            String bukkitMethods = String.format("sources/%s-method.s_", mcVersion);
            String mojang = String.format("sources/%s-all.m_", mcVersion);
            initialiseSpigot(bukkit, bukkitMethods);
            initialiseMC(mojang);

            BufferedReader reader = new BufferedReader(new FileReader(mojang));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMappingsFile(String prefix, String postfix) {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir + "/personoid/mappings/" + version.getName());
        if (!path.toFile().exists()) {
            return false;
        }
        File[] files = path.toFile().listFiles();
        if (files == null) {
            return false;
        }
        for (File file : files) {
            if (file.getName().startsWith(prefix) && file.getName().endsWith(postfix)) {
                return true;
            }
        }
        return false;
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

    public HashMap<String, MappedField> getClassFields() {
        return classFields;
    }

    public HashMap<String, MappedMethod> getClassMethods() {
        return classMethods;
    }

    public HashMap<String, MappedConstructor> getClassConstructors() {
        return classConstructors;
    }
}
