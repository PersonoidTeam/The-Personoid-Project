package com.personoid.nms;

import com.personoid.nms.packet.ReflectionUtils;

public enum MinecraftVersion {
    v1_19_3("1_19_3", 19, 3, "177811e1fa90f674897a302820f3ed84e4d65688", "bc44f6dd84cd2f3ad8c0caad850eaca9e82067e3"),
    v1_19_2("1_19_2", 19, 2, "d96ad8e1e64b7c35bb632339c23621353be1f028", "ed5e6e8334ad67f5af0150beed0f3d156d74bd57"),
    v1_19_1("1_19_1", 19, 1, "c540b6e228dc33c13c02b2af63a2691cda0cdea8", "3565648cdd47ae15738fb804a95a659137d7cfd3"),
    v1_19("1_19", 19, 0, "e6ebde42e39100b18ca0265596b04f557b2b27cc", "1c1cea17d5cd63d68356df2ef31e724dd09f8c26"),
    ;

    private static final MinecraftVersion version = MinecraftVersion.get();
    private final String name;
    private final int minor;
    private final int patch;
    private final String spigotCommit;
    private final String minecraftVersion;

    MinecraftVersion(String name, int minor, int patch, String spigotCommit, String minecraftVersion) {
        this.name = name;
        this.minor = minor;
        this.patch = patch;
        this.spigotCommit = spigotCommit;
        this.minecraftVersion = minecraftVersion;
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

    public String getSpigotCommit() {
        return spigotCommit;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public boolean isHigherThan(MinecraftVersion other) {
        return this.minor > other.minor || this.patch > other.patch;
    }

    public boolean isLowerThan(MinecraftVersion other) {
        return this.minor < other.minor || this.patch < other.patch;
    }

    public static MinecraftVersion get() {
        if (version != null) {
            return version;
        }
        String version = ReflectionUtils.getVersion();
        for (MinecraftVersion minecraftVersion : MinecraftVersion.values()) {
            if (minecraftVersion.getName().equals(version)) {
                return minecraftVersion;
            }
        }
        return null;
    }
}
