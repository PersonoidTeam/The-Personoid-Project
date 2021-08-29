package us.notnotdoddy.personoid.status;

public class Behavior {
    public enum Type {
        BUILDER(1),
        SPEEDRUNNER(1);

        int retention;
        Type(int retention) {
            this.retention = retention;
        }
    }

    public enum Mood {
        NEUTRAL,
        AGGRAVATED,
        ANGRY,
    }

    public enum MoodChangeType {
        NEUTRAL,
        ANGRY,
    }
}
