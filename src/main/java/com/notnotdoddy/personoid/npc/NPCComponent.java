package com.notnotdoddy.personoid.npc;

import java.util.Random;

public abstract class NPCComponent {
    protected final NPC npc;
    protected static final Random random = new Random();

    public NPCComponent(NPC npc) {
        this.npc = npc;
    }

    public NPC getNPC() {
        return npc;
    }
}
