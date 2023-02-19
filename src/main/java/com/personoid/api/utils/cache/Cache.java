package com.personoid.api.utils.cache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Cache {
    private final Map<String, Class<?>> classMap = new HashMap<>();
    private final Map<String, Object> objectMap = new HashMap<>();

    public Cache(String identifier) {

    }

    public Class<?> getClass(String key) {
        return classMap.get(key);
    }

    public <T> T get(String key) {
        return (T) objectMap.get(key);
    }

    public <T> T getOrPut(String key, Supplier<T> ifNullValue) {
        if (objectMap.containsKey(key)) return (T) objectMap.get(key);
        objectMap.put(key, ifNullValue.get());
        return ifNullValue.get();
    }

    public boolean contains(String key) {
        return classMap.containsKey(key) || objectMap.containsKey(key);
    }

    public void put(String key, Class<?> clazz) {
        classMap.put(key, clazz);
    }

    public void put(String key, Object value) {
        objectMap.put(key, value);
    }

    public void remove(String key) {
        classMap.remove(key);
        objectMap.remove(key);
    }

    public void save(String path) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            Path fullPath = Paths.get(tempDir + "/personoid/" + path + "/");
            if (!fullPath.toFile().exists()) {
                Files.createDirectories(fullPath);
            }
            for (Map.Entry<String, Class<?>> entry : classMap.entrySet()) {
                Path classesPath = Paths.get(fullPath.toString(), "/classes/");
                if (!classesPath.toFile().exists()) {
                    Files.createDirectories(classesPath);
                }
                Path classPath = Paths.get(classesPath.toString(), entry.getKey() + ".class");
                if (!classPath.toFile().exists()) {
                    Files.createFile(classPath);
                }
                serialize(entry.getValue(), classPath.toString());
            }
            for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                Path objectsPath = Paths.get(fullPath.toString(), "/objects/");
                if (!objectsPath.toFile().exists()) {
                    Files.createDirectories(objectsPath);
                }
                Path objectPath = Paths.get(objectsPath.toString(), entry.getKey() + ".object");
                if (!objectPath.toFile().exists()) {
                    Files.createFile(objectPath);
                }
                serialize(entry.getValue(), objectPath.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serialize(Object obj, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object deserialize(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void load(String path) {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path fullPath = Paths.get(tempDir + "/personoid/" + path);
        if (!fullPath.toFile().exists()) {
            return;
        }
        Path classesPath = Paths.get(fullPath.toString(), "/classes/");
        if (classesPath.toFile().exists()) {
            for (File file : classesPath.toFile().listFiles()) {
                // load bytes as class name
                try {
                    String className = (String) deserialize(file.toPath().toString());
                    Class<?> clazz = Class.forName(className);
                    classMap.put(file.getName().replace(".class", ""), clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Path objectsPath = Paths.get(fullPath.toString(), "/objects/");
        if (objectsPath.toFile().exists()) {
            for (File file : objectsPath.toFile().listFiles()) {
                try {
                    Object object = deserialize(file.toPath().toString());
                    objectMap.put(file.getName().replace(".object", ""), object);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
