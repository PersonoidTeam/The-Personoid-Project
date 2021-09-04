package us.notnotdoddy.personoid.status;

public class Behavior {
    private Type type;

    public Behavior(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        BUILDER(1, 0.3F),
        SPEEDRUNNER(1, 0.7F);

        public float retention;
        public float retentionDecrement;
        public float resourceGatheringSkill;

        Type(float retention, float resourceGatheringSkill) {
            this.resourceGatheringSkill = resourceGatheringSkill;
            this.retention = retention;
            retentionDecrement = (1 / retention) / 5000;
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
