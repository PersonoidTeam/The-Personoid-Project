package com.personoid.api.npc.injection;

public class CallbackInfo {
    private final Class<?> returnType;
    private Object returnValue;

    public CallbackInfo(Class<?> returnType) {
        this.returnType = returnType;
    }

    public void setReturnValue(Object value) {
        if (value.getClass() != returnType) {
            throw new IllegalArgumentException("Return type is not " + returnType.getSimpleName());
        }
        returnValue = value;
    }

    Object getReturnValue() {
        return returnValue;
    }

    Class<?> getReturnType() {
        return returnType;
    }
}
