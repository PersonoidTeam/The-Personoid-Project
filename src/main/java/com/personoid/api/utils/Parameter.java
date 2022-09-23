package com.personoid.api.utils;

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
}
