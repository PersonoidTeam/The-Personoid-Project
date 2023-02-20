package com.personoid.nms.mappings;

public class MappedField {
    private String type;
    private String obfuscatedName;

    public MappedField(String type, String obfuscatedName) {
        this.type = type;
        this.obfuscatedName = obfuscatedName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public void setObfuscatedName(String obfuscatedName) {
        this.obfuscatedName = obfuscatedName;
    }
}
