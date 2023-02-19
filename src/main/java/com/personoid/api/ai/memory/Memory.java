package com.personoid.api.ai.memory;

import java.util.HashSet;
import java.util.Set;

public class Memory<T> {
    private final String name;
    private final MemoryGroup group;
    private final T value;
    private final int forgetTime;
    private final Set<Memory<?>> links = new HashSet<>();

    private int forgetTimer;
    private boolean forgotten;

    public Memory(MemoryGroup group, String name, T value) {
        this(group, name, value, -1);
    }

    public Memory(MemoryGroup group, String name, T value, int forgetTime) {
        this.name = name;
        this.value = value;
        this.group = group;
        this.forgetTime = forgetTime;
    }

    public void tick() {
        if (forgetTime != -1) {
            forgetTimer++;
            if (forgetTimer >= forgetTime) {
                forget();
            }
        }
    }

    public void forget() {
        forgotten = true;
        for (Memory<?> memory : links) {
            memory.forgetTimer += forgetTime;
        }
        links.clear();
    }

    public void strengthen(int amount) {
        forgetTimer -= amount;
        if (forgetTimer < 0) {
            forgetTimer = 0;
        }
    }

    public String getName() {
        return name;
    }

    public MemoryGroup getGroup() {
        return group;
    }

    public T getValue() {
        return value;
    }

    public int getForgetTime() {
        return forgetTime;
    }

    public boolean isForgotten() {
        return forgotten;
    }

    public void addLink(Memory<?> memory) {
        links.add(memory);
    }

    public void removeLink(Memory<?> memory) {
        links.remove(memory);
    }

    public Set<Memory<?>> getLinks() {
        return links;
    }

    public boolean isLinked(Memory<?> memory) {
        return links.contains(memory);
    }

    public boolean isLinked(String name) {
        return links.stream().anyMatch(memory -> memory.getName().equals(name));
    }

    public boolean isLinked(MemoryGroup group) {
        return links.stream().anyMatch(memory -> memory.getGroup().equals(group));
    }

    public Set<Memory<?>> getLinks(String name) {
        Set<Memory<?>> memories = new HashSet<>();
        links.stream().filter(memory -> memory.getName().equals(name)).forEach(memories::add);
        return memories;
    }

    public Set<Memory<?>> getLinks(MemoryGroup group) {
        Set<Memory<?>> memories = new HashSet<>();
        links.stream().filter(memory -> memory.getGroup().equals(group)).forEach(memories::add);
        return memories;
    }

    public Set<Memory<?>> getLinks(MemoryGroup group, String name) {
        Set<Memory<?>> memories = new HashSet<>();
        links.stream().filter(memory -> memory.getGroup().equals(group) && memory.getName().equals(name)).forEach(memories::add);
        return memories;
    }
}
