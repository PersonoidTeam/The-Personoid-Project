package com.personoid.nms;

import com.personoid.nms.packet.NMSReflection;

public class MinecraftVersion {
    private static final MinecraftVersion version = MinecraftVersion.get();
    private final String name;
    private final int minor;
    private final int patch;

    private MinecraftVersion(String name) {
        this.name = name.replaceAll("[^0-9_]", "");
        String[] split = this.name.split("_");
        this.minor = Integer.parseInt(split[1]);
        this.patch = Integer.parseInt(split[2]);
    }

    public String getName() {
        return name;
    }

    public String getDotName() {
        return name.replace("_", ".");
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public boolean isHigherThan(MinecraftVersion other) {
        return this.minor > other.minor || this.patch > other.patch;
    }

    public boolean isLowerThan(MinecraftVersion other) {
        return this.minor < other.minor || this.patch < other.patch;
    }

    public static MinecraftVersion get() {
        if (version != null) return version;
        return new MinecraftVersion(NMSReflection.getVersion());
    }
}
