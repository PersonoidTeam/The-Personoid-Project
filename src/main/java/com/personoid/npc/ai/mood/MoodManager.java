package com.personoid.npc.ai.mood;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;

import java.util.HashSet;
import java.util.Set;

public class MoodManager extends NPCTickingComponent {
    private final Set<Mood> moods = initMoods();

    public MoodManager(NPC npc) {
        super(npc);
    }

    private Set<Mood> initMoods() {
        Set<Mood> moods = new HashSet<>();
        for (SelfMoodType mood : SelfMoodType.values()) {
            moods.add(new Mood(mood, 1)); // TODO: make this configurable via personality traits
        }
        return moods;
    }
}
