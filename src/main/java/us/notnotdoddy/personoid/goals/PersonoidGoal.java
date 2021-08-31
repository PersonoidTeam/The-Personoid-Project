package us.notnotdoddy.personoid.goals;

import us.notnotdoddy.personoid.npc.PersonoidNPC;

public abstract class PersonoidGoal {
    final boolean shouldOverrideExistingGoals;
    final GoalPriority goalPriority;

    public PersonoidGoal(boolean shouldOverrideExistingGoals, GoalPriority goalPriority) {
        this.shouldOverrideExistingGoals = shouldOverrideExistingGoals;
        this.goalPriority = goalPriority;
    }

    public boolean shouldOverrideExisting(){
        return shouldOverrideExistingGoals;
    }

    public GoalPriority getGoalPriority(){
        return goalPriority;
    }

    public abstract void initializeGoal(PersonoidNPC personoidNPC);

    public abstract void endGoal(PersonoidNPC personoidNPC);

    public abstract boolean canStart(PersonoidNPC personoidNPC);

    public abstract void tick(PersonoidNPC personoidNPC);

    public abstract boolean shouldStop(PersonoidNPC personoidNPC);


    // Made to account for alot of different goals, dont be shy to add in more priorities if you want.
    public enum GoalPriority {
        HIGH(1),
        MEDIUM(0.5),
        LOW(0)
        ;

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
