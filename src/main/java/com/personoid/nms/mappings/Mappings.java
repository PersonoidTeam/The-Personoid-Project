package com.personoid.nms.mappings;

import com.personoid.api.utils.bukkit.Logger;
import com.personoid.nms.MinecraftVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Mappings {
    private static final Mappings mappings = get();
    private final MinecraftVersion version = MinecraftVersion.get();
    private final MappingsDownloader downloader;
    private final MappingsLoader loader;

    public static Mappings get() {
        if (mappings != null) return mappings;
        return new Mappings();
    }

    private Mappings() {
        downloader = new MappingsDownloader(version);
        downloader.downloadMappings();
        loader = new MappingsLoader(version);
        loader.createMappings();
        Logger.get("Personoid").info("Finished initialising mappings!");
    }

    public String getMappedClassName(String clazz) {
        return loader.getSpigotClassName(clazz);
    }

    public Class<?> getMappedClass(String clazz) {
        try {
            return Class.forName(loader.getSpigotClassName(clazz));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Field getField(String clazz, String fieldName) {
        try {
            MappedClass mappedClass = loader.getClass(clazz);
            if (mappedClass == null) {
                throw new RuntimeException("Failed to find mapped class!");
            }
            MappedField mappedField = mappedClass.getFields().get(fieldName);
            if (mappedField == null) {
                throw new RuntimeException("Failed to find mapped field!");
            }
            Class<?> spigotClass = Class.forName(loader.getSpigotClassName(clazz));
            return spigotClass.getField(mappedField.getObfuscatedName());
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public Method getMethod(String clazz, String methodName, Class<?>... parameters) {
        try {
            MappedClass mappedClass = loader.getClass(clazz);
            if (mappedClass == null) {
                throw new RuntimeException("Failed to find mapped class!");
            }
            MappedMethod mappedMethod = mappedClass.getMethods().get(methodName);
            if (mappedMethod == null) {
                throw new RuntimeException("Failed to find mapped method!");
            }
            String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
            if (!Arrays.equals(mappedMethod.getArguments(), arguments)) {
                throw new RuntimeException("Method arguments of mapped method do not match!");
            }
            Class<?> spigotClass = Class.forName(loader.getSpigotClassName(clazz));
            return spigotClass.getMethod(mappedMethod.getObfuscatedName(), parameters);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public MinecraftVersion getVersion() {
        return version;
    }
}
