package com.personoid.npc.ai.activity;

import com.personoid.npc.NPC;
import com.personoid.npc.components.NPCTickingComponent;
import com.personoid.utils.debug.Profiler;

import java.util.*;

public class ActivityManager extends NPCTickingComponent {
    private final Set<Activity> registered = new HashSet<>();
    private final Queue<Activity> queue = new PriorityQueue<>(Collections.reverseOrder());
    private final Set<Activity> paused = new HashSet<>();
    private Activity current;

    public ActivityManager(NPC npc) {
        super(npc);
    }

    public void register(Activity... activities) {
        registered.addAll(Arrays.asList(activities));
    }

    public void register(Set<Activity> activities) {
        registered.addAll(activities);
    }

    public void register(List<Activity> activities) {
        registered.addAll(activities);
    }

    @Override
    public void tick() {
        if (current != null) {
            current.internalUpdate();
            current.onUpdate();
            if (current.isFinished()) {
                startNextActivity();
            } else if (current.canStop(Activity.StopType.STOP)) {
                current.internalStop(Activity.StopType.STOP);
                current.onStop(Activity.StopType.STOP);
                startNextActivity();
            } else if (!queue.isEmpty() && queue.peek().getPriority().isHigherThan(current.getPriority()) && current.canStop(Activity.StopType.PAUSE)) {
                current.internalStop(Activity.StopType.PAUSE);
                current.onStop(Activity.StopType.PAUSE);
                current.setPaused(true);
                paused.add(current);
            }
        } startNextActivity();
    }

    public boolean attemptStartPaused() {
        if (queue.isEmpty()) {
            for (Activity activity : paused) {
                if (activity.canStart(Activity.StartType.RESUME)) {
                    activity.internalStart(this, Activity.StartType.RESUME);
                    activity.onStart(Activity.StartType.RESUME);
                    activity.setPaused(false);
                    paused.remove(activity);
                    current = activity;
                    return true;
                }
            }
        } else if (!paused.isEmpty()) {
            for (Activity activity : paused) {
                if (activity.getPriority().isHigherThan(queue.peek().getPriority()) && activity.canStart(Activity.StartType.RESUME)) {
                    activity.internalStart(this, Activity.StartType.RESUME);
                    activity.onStart(Activity.StartType.RESUME);
                    activity.setPaused(false);
                    paused.remove(activity);
                    current = activity;
                    return true;
                }
            }
        } else queueIfEmpty();
        return false;
    }

    public void startNextActivity() {
        if (attemptStartPaused()) return;
        queueIfEmpty();
        current = queue.poll();
        if (current != null && current.canStart(Activity.StartType.START)) {
            current.internalStart(this, Activity.StartType.START);
            current.onStart(Activity.StartType.START);
        }
    }

    public void queueActivity(Activity activity) {
        queue.add(activity);
    }

    public void queueIfEmpty() {
        if (queue.isEmpty()) {
            Activity chosen = chooseViaPriority();
            if (chosen != null) queueActivity(chosen);
        }
    }

    public Activity chooseViaPriority() {
        List<Activity> canStart = new ArrayList<>();

        // Get all goals that can activate.
        for (Activity activity : registered){
            if (current != null){
                if (!activity.getPriority().isHigherThan(current.getPriority()) || activity.getPriority() != current.getPriority()){
                    continue;
                }
            }
            if (activity.canStart(Activity.StartType.START)){
                Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "can start " + activity.getClass().getSimpleName());
                canStart.add(activity);
            }
        }

        Activity highest = null;

        for (Activity activity : canStart){
            if (highest == null || activity.getPriority().isHigherThan(highest.getPriority())){
                highest = activity;
            }
        }

        if (highest != null) {
            Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "highest priority activity " + highest.getClass().getSimpleName());
            if (current != null) {
                if (highest.getPriority() == current.getPriority()) {
                    if (random.nextBoolean()){
                        Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "select activity random " + highest.getClass().getSimpleName());
                        return highest;
                    }
                }
            }
            else {
                Profiler.push(Profiler.Type.ACTIVITY_MANAGER, "select activity " + highest.getClass().getSimpleName());
                return highest;
            }
        }
        return null;
    }
}
