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
                if (argument.equals("int")) argumentTypes.add(int.class);
                else if (argument.equals("float")) argumentTypes.add(float.class);
                else if (argument.equals("double")) argumentTypes.add(double.class);
                else if (argument.equals("long")) argumentTypes.add(long.class);
                else if (argument.equals("short")) argumentTypes.add(short.class);
                else if (argument.equals("byte")) argumentTypes.add(byte.class);
                else if (argument.equals("boolean")) argumentTypes.add(boolean.class);
                else if (argument.equals("char")) argumentTypes.add(char.class);
                else if (argument.equals("void")) argumentTypes.add(void.class);
                else argumentTypes.add(Class.forName(argument));
            } catch (ClassNotFoundException e) {
                //throw new RuntimeException("Failed to find argument type!", e);
                argumentTypes.add(null);
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
