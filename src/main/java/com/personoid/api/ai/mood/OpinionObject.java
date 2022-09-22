package com.personoid.api.ai.mood;

import java.util.HashSet;
import java.util.Set;

public class OpinionObject {
    private final String identifier;
    private final Set<Mood> moods = initMoods();

    public OpinionObject(String identifier) {
        this.identifier = identifier;
    }

    private Set<Mood> initMoods() {
        Set<Mood> moods = new HashSet<>();
        for (MoodType mood : MoodType.values()) {
            moods.add(new Mood(mood, 1)); // TODO: make this configurable via personality traits
        }
        return moods;
    }

    public Mood getMood(MoodType mood) {
        for (Mood m : moods) {
            if (m.getMoodType() == mood) {
                return m;
            }
        }
        return null;
    }

    public String getIdentifier() {
        return identifier;
    }
}
