package com.personoid.nms.mappings;

import com.personoid.nms.MinecraftVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Mappings {
    private final MinecraftVersion version;
    private final MappingsLoader loader;

    public static Mappings get(MinecraftVersion version) {
        return new Mappings(version);
    }

    private Mappings(MinecraftVersion version) {
        this.version = version;
        this.loader = new MappingsLoader(version);
        loader.loadMappings();
    }

    public Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        MappedClass mappedClass = loader.getClasses().getOrDefault(clazz.getCanonicalName(), null);
        if (mappedClass == null) {
            return clazz.getField(fieldName);
        }
        MappedField mappedField = mappedClass.getFields().get(fieldName);
        if (mappedField == null) {
            return clazz.getField(fieldName);
        }
        return clazz.getField(mappedField.getObfuscatedName());
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        MappedClass mappedClass = loader.getClasses().getOrDefault(clazz.getCanonicalName(), null);
        if (mappedClass == null) {
            return clazz.getMethod(methodName, parameters);
        }
        MappedMethod mappedMethod = mappedClass.getMethods().get(methodName);
        if (mappedMethod == null) {
            return clazz.getMethod(methodName, parameters);
        }
        String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
        if (!Arrays.equals(mappedMethod.getArguments(), arguments)) {
            return clazz.getMethod(methodName, parameters);
        }
        return clazz.getMethod(mappedMethod.getObfuscatedName(), parameters);
    }

    public Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) throws NoSuchMethodException {
        return clazz.getConstructor(parameters);
    }

    public MinecraftVersion getVersion() {
        return version;
    }
}
