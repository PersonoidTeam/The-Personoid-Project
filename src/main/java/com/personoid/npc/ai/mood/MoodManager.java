package com.personoid.npc.ai.mood;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;

import java.util.HashSet;
import java.util.Set;

public class MoodManager extends NPCTickingComponent {
    private final Set<OpinionObject> opinions = new HashSet<>();

    public MoodManager(NPC npc) {
        super(npc);
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
