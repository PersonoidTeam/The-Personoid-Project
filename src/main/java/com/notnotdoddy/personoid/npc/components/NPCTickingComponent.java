package com.notnotdoddy.personoid.npc.components;

import com.notnotdoddy.personoid.npc.NPC;

public abstract class NPCTickingComponent extends NPCComponent {
    protected long currentTick;

    public NPCTickingComponent(NPC npc) {
        super(npc);
    }

    public void tick() {
        currentTick++;
    }
}
