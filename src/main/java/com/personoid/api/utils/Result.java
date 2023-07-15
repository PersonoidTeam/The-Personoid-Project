package com.personoid.api.utils;

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

    public T getResult() {
        return result;
    }

    public <Z> Z getResult(Class<Z> clazz) {
        if (clazz.isInstance(result)) {
            return clazz.cast(result);
        } else {
            throw new IllegalArgumentException("Result is not of type " + clazz.getSimpleName());
        }
    }

    public static <T> Result<T> success() {
        return new Result<>(Type.SUCCESS);
    }

    public static <T> Result<T> success(T result) {
        return new Result<>(Type.SUCCESS, result);
    }

    public static <T> Result<T> failure() {
        return new Result<>(Type.FAILURE);
    }

    public static <T> Result<T> failure(T result) {
        return new Result<>(Type.FAILURE, result);
    }

    public enum Type {
        SUCCESS,
        FAILURE,
    }
}
