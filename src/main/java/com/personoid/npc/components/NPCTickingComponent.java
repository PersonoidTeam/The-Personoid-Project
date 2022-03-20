package com.personoid.npc.components;

import com.personoid.npc.NPC;

public abstract class NPCTickingComponent extends NPCComponent {
    protected long currentTick;

    public NPCTickingComponent(NPC npc) {
        super(npc);
    }

    public void tick() {
        currentTick++;
    }
}
