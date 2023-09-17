package com.personoid.nms.mappings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NMSClass {
    private final String mojangName;
    private final String spigotName;
    private List<NMSConstructor> constructors;
    private final List<NMSMethod> methods = new ArrayList<>();
    private Map<String, NMSField> fields;

    public NMSClass(String mojangName, String spigotName) {
        this.mojangName = mojangName;
        this.spigotName = spigotName;
    }

    public NMSMethod getMethod(String methodName, Class<?>... parameters) {
        for (NMSMethod nmsMethod : methods) {
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
        if (fields == null) return null;
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
        this.constructors = constructors;
    }

    public List<NMSMethod> getMethods() {
        return methods;
    }

    void setMethods(List<NMSMethod> methods) {
        this.methods.addAll(methods);
    }

    public Map<String, NMSField> getFields() {
        return fields;
    }

    void setFields(Map<String, NMSField> fields) {
        this.fields = fields;
    }
}
