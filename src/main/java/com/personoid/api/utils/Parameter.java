package com.personoid.api.utils;

import java.lang.reflect.Array;

public class Parameter {
    private final Class<?> type;
    private final Object value;

    public Parameter(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValue(Class<? extends T> type) {
        return type.cast(value);
    }

    public Parameter array() {
        Object array = Array.newInstance(type, 1);
        Array.set(array, 0, value);
        return new Parameter(array.getClass(), array);
    }
}
