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
            Class<?>[] argTypesArray = argumentTypes.toArray(new Class<?>[0]);
            Method method = clazz.getRawClass().getMethod(obfuscatedName, argTypesArray);
            return (T) method.invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void findArgumentTypes() {
        for (String argument : arguments) {
            try {
                if (argument.equals("int")) argument = "java.lang.Integer";
                else if (argument.equals("float")) argument = "java.lang.Float";
                else if (argument.equals("double")) argument = "java.lang.Double";
                else if (argument.equals("long")) argument = "java.lang.Long";
                else if (argument.equals("short")) argument = "java.lang.Short";
                else if (argument.equals("byte")) argument = "java.lang.Byte";
                else if (argument.equals("boolean")) argument = "java.lang.Boolean";
                else if (argument.equals("char")) argument = "java.lang.Character";
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
