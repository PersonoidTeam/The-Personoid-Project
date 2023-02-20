package com.personoid.api.ai.memory;

import com.personoid.api.npc.NPC;

import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private final NPC npc;
    private final List<Memory<?>> memories = new ArrayList<>();

    public MemoryManager(NPC npc) {
        this.npc = npc;
    }

    public void tick() {
        memories.forEach(Memory::tick);
    }

    public void addMemory(Memory<?> memory) {
        memories.add(memory);
    }

    public void removeMemory(Memory<?> memory) {
        memories.remove(memory);
    }

    public void removeMemory(String name) {
        memories.removeIf(memory -> memory.getName().equals(name));
    }

    public void removeMemory(MemoryGroup group) {
        memories.removeIf(memory -> memory.getGroup().equals(group));
    }

    public void removeMemory(MemoryGroup group, String name) {
        memories.removeIf(memory -> memory.getGroup().equals(group) && memory.getName().equals(name));
    }

    public void getMemory(String name) {
        memories.stream().filter(memory -> memory.getName().equals(name)).findFirst();
    }

    public void getMemories(MemoryGroup group) {
        memories.stream().filter(memory -> memory.getGroup().equals(group));
    }

    public void getMemories(MemoryGroup group, String name) {
        memories.stream().filter(memory -> memory.getGroup().equals(group) && memory.getName().equals(name));
    }
}
