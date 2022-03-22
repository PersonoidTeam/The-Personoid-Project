package com.personoid.npc.ai.activity;

public class Result<T> {
    private final Type type;
    private final T result;

    public Result(Type type) {
        this.type = type;
        this.result = null;
    }

    public Result(Type type, T result) {
        this.type = type;
        this.result = result;
    }

    public Type getType() {
        return type;
    }

    public <Z> Z getResult(Class<Z> clazz) {
        if (clazz.isInstance(result)) {
            return clazz.cast(result);
        } else {
            throw new IllegalArgumentException("Result is not of type " + clazz.getSimpleName());
        }
    }

    public enum Type {
        SUCCESS,
        FAILURE
    }
}
