package com.personoid.api.ai.mood;

import com.personoid.api.npc.NPC;

import java.util.HashSet;
import java.util.Set;

public class MoodManager {
    private final NPC npc;
    private final Set<OpinionObject> opinions = new HashSet<>();

    public MoodManager(NPC npc) {
        this.npc = npc;
    }

    public void tick() {

    }

    public OpinionObject getOpinion(String identifier) {
        for (OpinionObject opinion : opinions) {
            if (opinion.getIdentifier().equals(identifier)) {
                return opinion;
            }
        }
        OpinionObject opinion = new OpinionObject(identifier);
        opinions.add(opinion);
        return opinion;
    }
}
