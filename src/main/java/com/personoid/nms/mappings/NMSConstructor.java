package com.personoid.nms.mappings;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class NMSConstructor {
    private final NMSClass clazz;
    private final String[] arguments;

    public NMSConstructor(NMSClass clazz, String[] arguments) {
        this.clazz = clazz;
        this.arguments = arguments;
    }

    public <T> T newInstance(Object... args) {
        try {
            Class<?>[] argTypesArray = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
            Constructor<?> constructor = clazz.getRawClass().getConstructor(argTypesArray);
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NMSClass getNMSClass() {
        return clazz;
    }

    public String[] getArguments() {
        return arguments;
    }
}
