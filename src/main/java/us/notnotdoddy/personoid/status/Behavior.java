package us.notnotdoddy.personoid.status;

public class Behavior {
    public enum Type {
        BUILDER(0.0001F),
        SPEEDRUNNER(0.0001F);

        public float retention;
        Type(float retention) {
            this.retention = retention;
        }
    }

    public enum Mood {
        NEUTRAL,
        HAPPY,
        SAD,
        ANGRY,
        GENEROUS,
        FEARFUL;
    }

    public static boolean isTarget(Mood mood, float strength) {
        if (mood == Mood.ANGRY) {
            return strength >= 0.5F;
        } else return false;
    }
}
