package com.personoid.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CacheManager {
    private final String identifier;
    private final Map<String, Class<?>> classMap = new HashMap<>();
    private final Map<String, Object> objectMap = new HashMap<>();

    public CacheManager(String identifier) {
        this.identifier = identifier;
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
}
