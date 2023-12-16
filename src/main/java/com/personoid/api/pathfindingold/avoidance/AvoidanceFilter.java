package com.personoid.api.pathfindingold.avoidance;

import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;

import java.util.HashMap;
import java.util.function.Predicate;

public class AvoidanceFilter<T> {
    private static final HashMap<Class<?>, AvoidanceFilter<?>> map = new HashMap<>();

    public static final AvoidanceFilter<Spider> SPIDER = new AvoidanceFilter<>(Spider.class, (spider) -> spider.getWorld().getTime() > 13000);
    public static final AvoidanceFilter<Slime> SLIME = new AvoidanceFilter<>(Slime.class, (slime) -> slime.getSize() > 1);
    public static final AvoidanceFilter<MagmaCube> MAGMA_CUBE = new AvoidanceFilter<>(MagmaCube.class, (magmaCube) -> magmaCube.getSize() > 1);

    private final Class<T> entityClass;
    private final Predicate<T> predicate;

    public AvoidanceFilter(Class<T> entityClass, Predicate<T> predicate) {
        this.entityClass = entityClass;
        this.predicate = predicate;
        registerFilter(this);
    }

    public boolean ignores(Entity entity) {
        if (predicate == null) return false;
        T type = entityClass.cast(entity);
        return !predicate.test(type);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    private static void registerFilter(AvoidanceFilter<?> filter) {
        map.put(filter.getEntityClass(), filter);
    }

    public static boolean shouldIgnore(Entity entity) {
        Class<? extends Entity> clazz = entity.getClass();
        AvoidanceFilter<?> filter = map.get(clazz);
        if (filter == null) return false;
        return filter.ignores(entity);
    }
}
