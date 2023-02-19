package com.personoid.api.ai.memory;

public class MemoryGroup {
    public static MemoryGroup DEFAULT = new MemoryGroup("default");
    public static MemoryGroup INTERACTION = new MemoryGroup("interaction");
    public static MemoryGroup POINT_OF_INTEREST = new MemoryGroup("point of interest");

    private final String name;

    public MemoryGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
