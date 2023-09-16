package com.personoid.nms.mappings;

import com.personoid.api.utils.bukkit.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NMSClass {
    private final String mojangName;
    private final String spigotName;
    private List<NMSConstructor> constructors;
    private Map<String, List<NMSMethod>> methods;
    private Map<String, NMSField> fields;

    public NMSClass(String mojangName, String spigotName) {
        this.mojangName = mojangName;
        this.spigotName = spigotName;
    }

    public NMSMethod getMethod(String methodName, Class<?>... parameters) {
        Logger.get().severe("looking for method: " + methodName);
        if (methods != null) {
            List<NMSMethod> nmsMethods = methods.get(methodName);
            if (nmsMethods != null) {
                Logger.get().severe("finding method...");
                for (NMSMethod nmsMethod : nmsMethods) {
                    if (parameters.length > 0 && nmsMethod.getArguments().length > 0) {
                        String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
                        if (!Arrays.equals(nmsMethod.getArguments(), arguments)) continue;
                    }
                    if (spigotName.equals("net.minecraft.world.entity.Entity")) {
                        Logger.get().severe("found method: " + nmsMethod.getMojangName());
                        if (parameters.length > 0 && nmsMethod.getArguments().length > 0) {
                            String[] arguments = Arrays.stream(parameters).map(Class::getCanonicalName).toArray(String[]::new);
                            Logger.get().severe("arguments: " + Arrays.toString(arguments));
                            Logger.get().severe("NMSMethod arguments: " + Arrays.toString(nmsMethod.getArguments()));
                        } else {
                            Logger.get().severe("no arguments");
                        }
                    }
                    return nmsMethod;
                }
                Logger.get().severe("finished finding method");
            } else {
                Logger.get().severe("no method found");
            }
        } else {
            if (spigotName.equals("net.minecraft.world.entity.Entity")) {
                Logger.get().severe("methods is null");
            }
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
        //Logger.get().severe("raw class: " + getRawClass());
        //Logger.get().severe("superclass: " + getRawClass().getSuperclass());
        if (superclass == null) return null;
        //Logger.get().severe("spigot: " + Mappings.get().getClassFromSpigot(superclass.getCanonicalName()));
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

    public Map<String, List<NMSMethod>> getMethods() {
        return methods;
    }

    void setMethods(Map<String, List<NMSMethod>> methods) {
        this.methods = methods;
    }

    public Map<String, NMSField> getFields() {
        return fields;
    }

    void setFields(Map<String, NMSField> fields) {
        this.fields = fields;
    }
}
