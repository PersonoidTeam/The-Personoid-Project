package com.personoid.nms.mappings;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.personoid.api.utils.Parameter;

import java.lang.reflect.Method;
import java.util.*;

public class NMSClass {
    private final String mojangName;
    private final String spigotName;

    private final List<NMSConstructor> constructors = new ArrayList<>();
    private final ListMultimap<String, NMSMethod> methods = ArrayListMultimap.create();
    private final Map<String, NMSField> fields = new HashMap<>();

    public NMSClass(String mojangName, String spigotName) {
        this.mojangName = mojangName;
        this.spigotName = spigotName;
    }

    public Object construct(Parameter... args) {
        Object argValues = Arrays.stream(args).map(Parameter::getValue).toArray();
        for (NMSConstructor constructor : constructors) {
            if (constructor.getArguments().length != args.length) continue;
            boolean matches = true;
            for (int i = 0; i < args.length; i++) {
                if (!constructor.getArguments()[i].equals(args[i].getType().getCanonicalName())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return constructor.newInstance(argValues);
            }
        }
        try {
            Class<?>[] argTypes = Arrays.stream(args).map(Parameter::getType).toArray(Class<?>[]::new);
            return getRawClass().getConstructor(argTypes).newInstance(argValues);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NMSConstructor getConstructor(Class<?>... args) {
        for (NMSConstructor constructor : constructors) {
            if (constructor.getArguments().length != args.length) continue;
            boolean matches = true;
            for (int i = 0; i < args.length; i++) {
                if (!constructor.getArguments()[i].equals(args[i].getCanonicalName())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return constructor;
            }
        }
        String[] argNames = Arrays.stream(args).map(Class::getCanonicalName).toArray(String[]::new);
        return new NMSConstructor(this, argNames);
    }

    public NMSMethod getMethod(String methodName, Class<?>... parameters) {
        List<NMSMethod> thisMethod = methods.get(methodName);
        for (NMSMethod nmsMethod : thisMethod) {
            if (!nmsMethod.getMojangName().equals(methodName)) continue;
            if (parameters.length > 0 && nmsMethod.getArguments().length > 0) {
                String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
                if (!Arrays.equals(nmsMethod.getArguments(), arguments)) continue;
            }
            return nmsMethod;
        }
        try {
            Method method = getRawClass().getMethod(methodName, parameters);
            return new NMSMethod(this, method.getName(), method.getName(),
                    method.getReturnType().getCanonicalName(),
                    Arrays.stream(method.getParameterTypes()).map(Class::getCanonicalName).toArray(String[]::new));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public NMSField getField(String fieldName) {
        NMSField field = fields.get(fieldName);
        if (field != null) return field;
        try {
            return new NMSField(this, fieldName, fieldName,
                    getRawClass().getDeclaredField(fieldName).getType().getCanonicalName());
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public Class<?> getRawClass() {
        try {
            return Class.forName(spigotName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public NMSClass getSuperclass() {
        Class<?> superclass = getRawClass().getSuperclass();
        if (superclass == null) return null;
        return Mappings.get().getClassFromSpigot(superclass.getCanonicalName());
    }

    public String getMojangName() {
        return mojangName;
    }

    public String getSpigotName() {
        return spigotName;
    }

    public List<NMSConstructor> getConstructors() {
        return constructors;
    }

    void setConstructors(List<NMSConstructor> constructors) {
        this.constructors.addAll(constructors);
    }

    public ListMultimap<String, NMSMethod> getMethods() {
        return methods;
    }

    void setMethods(ListMultimap<String, NMSMethod> methods) {
        this.methods.putAll(methods);
    }

    public Map<String, NMSField> getFields() {
        return fields;
    }

    void setFields(Map<String, NMSField> fields) {
        this.fields.putAll(fields);
    }
}
