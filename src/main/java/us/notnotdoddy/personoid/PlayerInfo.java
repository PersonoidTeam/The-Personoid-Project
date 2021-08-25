package us.notnotdoddy.personoid;

import org.bukkit.entity.Player;

public class PlayerInfo {
    public Player player;
    public Behavior.Mood mood = Behavior.Mood.NEUTRAL;
    public int killedBy = 0;
    public int killed = 0;

    public PlayerInfo(Player player) {
        this.player = player;
    }

    public Behavior.Mood getNextMood(Behavior.MoodChangeType change) {
        if (mood == Behavior.Mood.NEUTRAL) {
            if (change == Behavior.MoodChangeType.ANGRY) {
                return Behavior.Mood.AGGRAVATED;
            }
            return Behavior.Mood.NEUTRAL;
        } else if (mood == Behavior.Mood.AGGRAVATED) {
            if (change == Behavior.MoodChangeType.ANGRY) {
                return Behavior.Mood.ANGRY;
            }
            return Behavior.Mood.NEUTRAL;
        } else if (mood == Behavior.Mood.ANGRY) {
            if (change == Behavior.MoodChangeType.ANGRY) {
                return Behavior.Mood.ANGRY;
            }
            return Behavior.Mood.AGGRAVATED;
        }
        return Behavior.Mood.NEUTRAL;
    }

    public enum DamageCause {

    }
}
