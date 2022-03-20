package com.notnotdoddy.personoid.npc;

public abstract class NPCTickingComponent extends NPCComponent {
    protected long currentTick;

    public NPCTickingComponent(NPC npc) {
        super(npc);
    }

    public void tick() {
        currentTick++;
    }
}
