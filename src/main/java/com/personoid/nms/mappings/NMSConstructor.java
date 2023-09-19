package com.personoid.nms.mappings;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class NMSConstructor {
    private final NMSClass clazz;
    private final String[] arguments;

    private final List<Class<?>> argumentTypes = new ArrayList<>();

    public NMSConstructor(NMSClass clazz, String[] arguments) {
        this.clazz = clazz;
        this.arguments = arguments;

        findArgumentTypes();
    }

    public <T> T newInstance(Object... args) {
        try {
            Class<?>[] argTypes = argumentTypes.toArray(new Class[0]);
            Constructor<?> constructor = clazz.getRawClass().getConstructor(argTypes);
            return (T) constructor.newInstance(args);
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
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                //throw new RuntimeException("Failed to find argument type!", e);
                argumentTypes.add(null);
            }
        }
    }

    public NMSClass getNMSClass() {
        return clazz;
    }

    public String[] getArguments() {
        return arguments;
    }
}
