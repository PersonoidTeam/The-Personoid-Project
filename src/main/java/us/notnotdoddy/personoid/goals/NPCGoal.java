package us.notnotdoddy.personoid.goals;

import us.notnotdoddy.personoid.npc.PersonoidNPC;

public abstract class NPCGoal {
    final boolean overrideExistingGoals;
    final GoalPriority priority;

    public NPCGoal(boolean overrideExistingGoals, GoalPriority priority) {
        this.overrideExistingGoals = overrideExistingGoals;
        this.priority = priority;
    }

    public boolean shouldOverrideExisting(){
        return overrideExistingGoals;
    }

    public GoalPriority getPriority(){
        return priority;
    }

    public abstract void initializeGoal(PersonoidNPC personoidNPC);

    public abstract void endGoal(PersonoidNPC personoidNPC);

    public abstract boolean canStart(PersonoidNPC personoidNPC);

    public abstract void tick(PersonoidNPC personoidNPC);

    public abstract boolean shouldStop(PersonoidNPC personoidNPC);


    // Made to account for alot of different goals, dont be shy to add in more priorities if you want.
    public enum GoalPriority {
        HIGHEST(1),
        HIGH(0.75),
        MEDIUM(0.5),
        LOW(0.25),
        LOWEST(0);

        final double doubleValue;

        GoalPriority(double doubleValue) {
            this.doubleValue = doubleValue;
        }

        private double getDouble(){
            return doubleValue;
        }

        public boolean isHigherThan(GoalPriority goalPriority){
            return doubleValue > goalPriority.getDouble();
        }
    }
}
