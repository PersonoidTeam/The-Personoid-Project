package us.notnotdoddy.personoid.utils;

import us.notnotdoddy.personoid.status.Behavior;

import java.util.HashMap;
import java.util.Map;

public class PlayerInfo {
    private final Map<Behavior.Mood, Float> moods = new HashMap<>();
    public int killedBy;
    public int killed;

    public PlayerInfo() {
        for (Behavior.Mood mood : Behavior.Mood.values()) {
            moods.put(mood, mood == Behavior.Mood.NEUTRAL ? 1F : 0F);
        }
    }

    public void setMood(Behavior.Mood mood, float strength) {
        moods.replace(mood, strength);
    }

    public void incrementMoodStrength(Behavior.Mood mood, float value) {
        moods.replace(mood, Math.min(Math.max(moods.get(mood) + value, 0F), 1F));
    }

    public void decrementMoodStrength(Behavior.Mood mood, float value) {
        moods.replace(mood, Math.min(Math.max(moods.get(mood) - value, 0F), 1F));
    }

    public void multiplyMoodStrength(Behavior.Mood mood, float value) {
        moods.replace(mood, Math.min(Math.max(moods.get(mood) * value, 0F), 1F));
    }

    public void divideMoodStrength(Behavior.Mood mood, float value) {
        moods.replace(mood, Math.min(Math.max(moods.get(mood) / value, 0F), 1F));
    }

    public float getMoodValue(Behavior.Mood mood){
        return moods.get(mood);
    }

    public Behavior.Mood getStrongestMood() {
        float highestStrength = 0;
        Behavior.Mood highestMood = Behavior.Mood.NEUTRAL;
        for (Map.Entry<Behavior.Mood, Float> entry : moods.entrySet()) {
            if (entry.getValue() > highestStrength) {
                highestMood = entry.getKey();
            }
        }
        return highestMood;
    }

    public boolean isTarget() {
        for (Map.Entry<Behavior.Mood, Float> entry : moods.entrySet()) {
            if (Behavior.isTarget(entry.getKey(), entry.getValue())) {
                return true;
            }
        }
        return false;
    }
}
