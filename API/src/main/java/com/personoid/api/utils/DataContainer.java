package com.personoid.api.utils;

import java.util.HashMap;

public class DataContainer {
    private final HashMap<String, Object> data = new HashMap<>();

    /**Stores the specified value in the container**/
    public <T> void store(String identifier, T data) {
        this.data.put(identifier, data);
    }

    /**Retrieves the stored value from the container**/
    public <T> T retrieve(String identifier, Class<T> type) {
        return get(identifier, type, false);
    }

    /**Retrieves the stored value from the container**/
    public <T> T retrieve(String identifier) {
        return get(identifier, false);
    }

    /**Removes and returns the stored value from the container**/
    public <T> T collect(String identifier, Class<T> type) {
        return get(identifier, type, true);
    }

    /**Removes and returns the stored value from the container**/
    public <T> T collect(String identifier) {
        return get(identifier, true);
    }

    /**Removes the stored value from the container**/
    public void remove(String identifier) {
        data.remove(identifier);
    }

    private <T> T get(String identifier, boolean remove) {
        Object value = data.get(identifier);
        try {
            if (remove) data.remove(identifier);
            return (T) value;
        } catch (Exception e) {
            throw new RuntimeException("[" + getClass().getSimpleName() + "] Invalid cast (" + identifier + ")");
        }
    }

    private <T> T get(String identifier, Class<T> type, boolean remove) {
        Object value = data.get(identifier);
        if (type.isInstance(value)) {
            if (remove) data.remove(identifier);
            return (T) value;
        } else {
            throw new RuntimeException("[" + getClass().getSimpleName() + "] Invalid cast to type " +
                    type.getSimpleName() + " (" + identifier + ")");
        }
    }

    public void wipe() {
        data.clear();
    }
}
