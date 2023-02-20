package com.personoid.nms.mappings;

public class MappedMethod {
    private String obfuscatedName;
    private String[] arguments;

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    private String returnType;

    public MappedMethod(String obfuscatedName, String returnType, String[] arguments) {
        this.obfuscatedName = obfuscatedName;
        this.returnType = returnType;
        this.arguments = arguments;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public void setObfuscatedName(String obfuscatedName) {
        this.obfuscatedName = obfuscatedName;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
}
