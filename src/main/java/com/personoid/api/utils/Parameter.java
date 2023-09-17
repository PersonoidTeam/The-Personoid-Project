package com.personoid.api.utils;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class Parameter {
    private final Class<?> type;
    private final Object value;

    public Parameter(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static Parameter of(Object value) {
        return new Parameter(value.getClass(), value);
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

    public Parameter list() {
        return new Parameter(List.class, Collections.singletonList(value));
    }

    public Parameter enumSet() {
        if (value instanceof Enum<?>) {
            return new Parameter(EnumSet.class, EnumSet.of((Enum) value));
        } else {
            throw new IllegalArgumentException("Value must be an enum");
        }
    }
}
