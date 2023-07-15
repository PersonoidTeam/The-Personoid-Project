package com.personoid.nms.mappings;

import com.personoid.api.utils.bukkit.Logger;
import com.personoid.nms.MinecraftVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class Mappings {
    private static final Mappings mappings = get();
    private final MinecraftVersion version = MinecraftVersion.get();
    private final MappingsLoader loader;

    public static Mappings get() {
        if (mappings != null) return mappings;
        return new Mappings();
    }

    private Mappings() {
        loader = new MappingsLoader(version);
        loader.loadMappings();
    }

    public Field getField(Class<?> clazz, String fieldName) {
        try {
            MappedClass mappedClass = loader.getClasses().get(clazz.getCanonicalName());
            if (mappedClass == null) {
                return clazz.getField(fieldName);
            }
            MappedField mappedField = mappedClass.getFields().get(fieldName);
            if (mappedField == null) {
                return clazz.getField(fieldName);
            }
            return clazz.getField(mappedField.getObfuscatedName());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Logger.get().info("class: " + clazz.getCanonicalName());
            MappedClass mappedClass = loader.getClasses().get(clazz.getCanonicalName());
            if (mappedClass == null) {
                Logger.get().info("class is null");
                return clazz.getMethod(methodName, parameters);
            }
            MappedMethod mappedMethod = mappedClass.getMethods().get(methodName);
            if (mappedMethod == null) {
                Logger.get().info("method is null");
                return clazz.getMethod(methodName, parameters);
            }
            Logger.get().info("method: " + mappedMethod.getObfuscatedName());
            String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
            if (!Arrays.equals(mappedMethod.getArguments(), arguments)) {
                Logger.get().info("arguments do not match");
                return clazz.getMethod(methodName, parameters);
            }
            return clazz.getMethod(mappedMethod.getObfuscatedName(), parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Method getMethod(String clazz, String methodName, Class<?>... parameters) {
        Logger.get().severe("mappings: " + loader.getClasses().size());
        for (Map.Entry<String, MappedClass> mapped : loader.getClasses().entrySet()) {
            if (mapped.getKey().contains("LivingEntity")) {
                Logger.get().severe("CLASS: " + mapped.getKey());
            }
        }
        try {
            MappedClass mappedClass = loader.getClasses().get(clazz);
            if (mappedClass == null) {
                Logger.get().info("class is null");
                return null;
            }
            MappedMethod mappedMethod = mappedClass.getMethods().get(methodName);
            Logger.get().severe("LOGGING METHODS...");
            for (Map.Entry<String, MappedMethod> method : mappedClass.getMethods().entrySet()) {
                Logger.get().severe(method.getKey());
            }
            if (mappedMethod == null) {
                Logger.get().info("method is null");
                return null;
            }
            Logger.get().info("method: " + mappedMethod.getObfuscatedName());
            String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
            if (!Arrays.equals(mappedMethod.getArguments(), arguments)) {
                Logger.get().info("arguments do not match");
                return null;
            }
            return Class.forName(clazz).getMethod(mappedMethod.getObfuscatedName(), parameters);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public MinecraftVersion getVersion() {
        return version;
    }
}
