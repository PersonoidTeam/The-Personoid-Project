package com.notnotdoddy.personoid.npc.components;

import com.notnotdoddy.personoid.npc.NPC;

import java.util.Random;

public abstract class NPCComponent extends DataHandler {
    protected final NPC npc;
    protected static final Random random = new Random();

    public NPCComponent(NPC npc) {
        this.npc = npc;
    }

    public NPC getNPC() {
        return npc;
    }
}
