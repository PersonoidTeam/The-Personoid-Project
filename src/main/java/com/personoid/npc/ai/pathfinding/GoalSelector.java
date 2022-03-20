package com.personoid.npc.ai.pathfinding;

import com.personoid.npc.NPC;
import com.personoid.npc.ai.pathfinding.goals.PathfinderGoal;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.debug.Profiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoalSelector extends NPCTickingComponent {
    private final List<PathfinderGoal> goals = new ArrayList<>();
    private PathfinderGoal current;

    public GoalSelector(NPC npc) {
        super(npc);
    }

    public void registerGoals(PathfinderGoal... goals) {
        this.goals.addAll(Arrays.stream(goals).toList());
        for (PathfinderGoal pathfinderGoal : this.goals){
            Profiler.push(Profiler.Type.GOAL_SELECTION, "registered goal " + pathfinderGoal.getClass().getSimpleName());
        }
    }

    public void unregisterGoals(PathfinderGoal... goals) {
        this.goals.removeAll(Arrays.stream(goals).toList());
    }

    public PathfinderGoal getCurrentGoal() {
        return current;
    }

    @Override
    public void tick() {
        super.tick();
        if (current != null){
            current.onUpdate();
            if (current.canStop(new PathfinderGoal.StopInfo(PathfinderGoal.StateReason.CHECK))){
                current.onStop();
                current = null;
            }
        }
        checkGoals();
    }

    private void checkGoals(){
        List<PathfinderGoal> goalsThatCanActivate = new ArrayList<>();

        boolean hasActiveGoal = current != null;

        // Get all goals that can activate.
        for (PathfinderGoal pathfinderGoal : goals){
            if (hasActiveGoal){
                if (!pathfinderGoal.getPriority().isHigherThan(current.getPriority()) || pathfinderGoal.getPriority() != current.getPriority()){
                    continue;
                }
            }
            if (pathfinderGoal.canStart(new PathfinderGoal.StartInfo(PathfinderGoal.StateReason.CHECK))){
                Profiler.push(Profiler.Type.GOAL_SELECTION, "can start " + pathfinderGoal.getClass().getSimpleName());
                goalsThatCanActivate.add(pathfinderGoal);
            }
        }

        PathfinderGoal highestPriorityGoal = null;

        for (PathfinderGoal pathfinderGoal : goalsThatCanActivate){
            if (highestPriorityGoal == null || pathfinderGoal.getPriority().isHigherThan(highestPriorityGoal.getPriority())){
                highestPriorityGoal = pathfinderGoal;
            }
        }

        if (highestPriorityGoal != null) {
            Profiler.push(Profiler.Type.GOAL_SELECTION, "highest priority goal " + highestPriorityGoal.getClass().getSimpleName());
            if (current != null) {
                if (highestPriorityGoal.getPriority() == current.getPriority()) {
                    if (random.nextBoolean()){
                        Profiler.push(Profiler.Type.GOAL_SELECTION, "select goal random " + highestPriorityGoal.getClass().getSimpleName());
                        selectGoal(highestPriorityGoal.getClass());
                    }
                }
            }
            else {
                Profiler.push(Profiler.Type.GOAL_SELECTION, "select goal " + highestPriorityGoal.getClass().getSimpleName());
                selectGoal(highestPriorityGoal.getClass());
            }
        }
    }

    private void selectGoal(Class<? extends PathfinderGoal> goal){
        // TODO: what if there are multiple goals of the same class?
        PathfinderGoal pathfinderGoal = null;

        for (PathfinderGoal pathGoal : goals){
            if (pathGoal.getClass().equals(goal)){
                pathfinderGoal = pathGoal;
                Profiler.push(Profiler.Type.GOAL_SELECTION, "found similar goal " + pathfinderGoal.getClass().getSimpleName());
                break;
            }
        }

        if (pathfinderGoal != null){
            if (current != null){
                current.onStop();
            }
            current = pathfinderGoal;
            Profiler.push(Profiler.Type.GOAL_SELECTION, "start goal " + current.getClass().getSimpleName());
            current.initParameters();
            current.onStart();
        }
    }
}
