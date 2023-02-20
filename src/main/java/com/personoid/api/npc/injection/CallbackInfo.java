package com.personoid.api.npc.injection;

public class CallbackInfo<T> {
    private final Class<?> returnType;
    private Object returnValue;

    public CallbackInfo(Class<T> returnType) {
        this.returnType = returnType;
    }

    public void setReturnValue(T value) {
        returnValue = value;
    }

    Object getReturnValue() {
        return returnValue;
    }

    Class<?> getReturnType() {
        return returnType;
    }
}
