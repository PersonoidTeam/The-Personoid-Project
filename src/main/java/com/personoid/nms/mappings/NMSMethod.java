package com.personoid.nms.mappings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NMSMethod {
    private final NMSClass clazz;
    private final String mojangName;
    private final String obfuscatedName;
    private final String[] arguments;
    private final String returnType;

    private final List<Class<?>> argumentTypes = new ArrayList<>();

    public NMSMethod(NMSClass clazz,
                     String mojangName,
                     String obfuscatedName,
                     String returnType,
                     String[] arguments) {
        this.clazz = clazz;
        this.mojangName = mojangName;
        this.obfuscatedName = obfuscatedName;
        this.returnType = returnType;
        this.arguments = arguments;

        findArgumentTypes();
    }

    public <T> T invoke(Object instance, Object... args) {
        try {
            Class<?>[] argTypes = argumentTypes.toArray(new Class[0]);
            Method method = clazz.getRawClass().getMethod(obfuscatedName, argTypes);
            return (T) method.invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private void findArgumentTypes() {
        for (String argument : arguments) {
            try {
                // handle primitive types
                switch (argument) {
                    case "byte":
                        argumentTypes.add(byte.class);
                        continue;
                    case "short":
                        argumentTypes.add(short.class);
                        continue;
                    case "int":
                        argumentTypes.add(int.class);
                        continue;
                    case "long":
                        argumentTypes.add(long.class);
                        continue;
                    case "float":
                        argumentTypes.add(float.class);
                        continue;
                    case "double":
                        argumentTypes.add(double.class);
                        continue;
                    case "boolean":
                        argumentTypes.add(boolean.class);
                        continue;
                    case "char":
                        argumentTypes.add(char.class);
                        continue;
                }
                argumentTypes.add(Class.forName(argument));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to find argument type!", e);
            }
        }
    }

    public NMSClass getNMSClass() {
        return clazz;
    }

    public String getMojangName() {
        return mojangName;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getReturnType() {
        return returnType;
    }
}
