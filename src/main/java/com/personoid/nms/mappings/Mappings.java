package com.personoid.nms.mappings;

import com.personoid.api.utils.bukkit.Logger;
import com.personoid.nms.MinecraftVersion;

public class Mappings {
    private static Mappings instance;
    private final MinecraftVersion version = MinecraftVersion.get();
    private final MappingsDownloader downloader;
    private final MappingsLoader loader;

    public static Mappings get() {
        if (instance == null) instance = new Mappings();
        return instance;
    }

    private Mappings() {
        downloader = new MappingsDownloader(version);
        downloader.downloadMappings();
        loader = new MappingsLoader(version);
        loader.createMappings();
        Logger.get("Personoid").info("Finished initialising mappings (version: " + version.getDotName() + ")");
    }

    public String getSpigotClassName(String mojangClassName) {
        return loader.getSpigotClassName(mojangClassName);
    }

    public String getMojangClassName(String spigotClassName) {
        return loader.getMojangClassName(spigotClassName);
    }

    public NMSClass getClassFromMojang(String clazz) {
        NMSClass nmsClass = loader.getClass(clazz, false);
        if (nmsClass != null) return nmsClass;
        if (clazz.startsWith("net.minecraft.")) return null;
        return loader.createRawMapping(clazz);
    }

    public NMSClass getClassFromSpigot(String clazz) {
        NMSClass nmsClass = loader.getClass(clazz, true);
        if (nmsClass != null) return nmsClass;
        if (clazz.startsWith("net.minecraft.")) return null;
        return loader.createRawMapping(clazz);
    }

    public MinecraftVersion getVersion() {
        return version;
    }

    public MappingsDownloader getDownloader() {
        return downloader;
    }

    public MappingsLoader getLoader() {
        return loader;
    }
}
