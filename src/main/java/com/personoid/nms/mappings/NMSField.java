package com.personoid.nms.mappings;

public class NMSField {
    private final NMSClass clazz;
    private final String type;
    private final String mojangName;
    private final String obfuscatedName;

    public NMSField(NMSClass clazz,
                    String type,
                    String mojangName,
                    String obfuscatedName) {
        this.clazz = clazz;
        this.type = type;
        this.mojangName = mojangName;
        this.obfuscatedName = obfuscatedName;
    }

    public <T> T getValue(Object instance) {
        try {
            return (T) clazz.getRawClass().getField(obfuscatedName).get(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T getStaticValue() {
        return getValue(null);
    }

    public void set(Object instance, Object value) {
        try {
            clazz.getRawClass().getField(obfuscatedName).set(instance, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public NMSClass getNMSClass() {
        return clazz;
    }

    public String getType() {
        return type;
    }

    public String getMojangName() {
        return mojangName;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }
}
