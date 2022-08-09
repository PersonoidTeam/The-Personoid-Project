package com.personoid.api.ai.activity;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.debug.Profiler;

import java.util.*;

public class ActivityManager {
    private final NPC npc;
    private final Set<Activity> registered = new HashSet<>();
    private final Queue<Activity> queue = new PriorityQueue<>(Collections.reverseOrder());
    private final Set<Activity> paused = new HashSet<>();
    private final HashMap<Activity, Integer> boredTasks = new HashMap<>();
    private Activity current;

    public ActivityManager(NPC npc) {
        this.npc = npc;
    }

    public void register(Activity... activities) {
        registered.addAll(Arrays.asList(activities));
    }

    public void unregister(Activity... activities) {
        Arrays.asList(activities).forEach(registered::remove);
    }

    public void unregister(boolean stop, Activity... activities) {
        Arrays.asList(activities).forEach(activity -> {
            if (stop && current == activity) {
                current.internalStop(Activity.StopType.STOP);
                current.onStop(Activity.StopType.STOP);
            }
        });
    }

    public void tick() {
        for (Activity activity : boredTasks.keySet()){
            boredTasks.put(activity, boredTasks.get(activity)-1);
            if (boredTasks.get(activity) <= 0){
                boredTasks.remove(activity);
                Profiler.ACTIVITIES.push("Removed bored cooldown for: " + activity.getClass().getSimpleName());
            }
        }
        if (current != null) {
            if (current.isFinished()) {
                current = null;
            } else if (!queue.isEmpty() && queue.peek().getPriority().isHigherThan(current.getPriority()) && current.canStop(Activity.StopType.PAUSE)) {
                current.internalStop(Activity.StopType.PAUSE);
                current.onStop(Activity.StopType.PAUSE);
                current.setPaused(true);
                paused.add(current);
            } else {
                current.internalUpdate();
                current.onUpdate();
            }
        } else startNextActivity();
    }

    private void startNextActivity() {
        Profiler.ACTIVITIES.push("Starting next activity");
        if (!paused.isEmpty()) {
            for (Activity activity : paused) {
                boolean higherThanNext = queue.isEmpty() || activity.getPriority().isHigherThan(queue.peek().getPriority());
                activity.register(this);
                if (higherThanNext && activity.canStart(Activity.StartType.RESUME)) {
                    startActivity(activity, Activity.StartType.RESUME);
                    Profiler.ACTIVITIES.push("attempt start paused true");
                    return;
                }
            }
        }
        queueIfEmpty();
        Activity next = queue.poll();
        if (next != null) {
            next.register(this);
            if (next.canStart(Activity.StartType.START)) {
                startActivity(next, Activity.StartType.START);
            }
        }
    }

    public void startActivity(Activity activity, Activity.StartType startType) {
        Profiler.ACTIVITIES.push("Starting activity " + activity.getClass().getSimpleName());
        activity.internalStart(startType);
        activity.onStart(startType);
        if (startType == Activity.StartType.RESUME) {
            activity.setPaused(false);
            paused.remove(activity);
        }
        current = activity;
    }

    public void queueActivity(Activity activity) {
        queue.add(activity);
    }

    private void queueIfEmpty() {
        if (queue.isEmpty()) {
            Activity chosen = chooseViaPriority();
            if (chosen != null) queueActivity(chosen);
        }
    }

    NPC getNPC() {
        return npc;
    }

    void addBoredTask(Activity activity, int cooldown) {
        boredTasks.put(activity, cooldown);
    }

    private Activity chooseViaPriority() {
        Set<Activity> canStart = new HashSet<>();
        // Get all goals that can activate.
        for (Activity activity : registered){
            if (boredTasks.containsKey(activity)) continue;
            if (current != null){
                if (!activity.getPriority().isHigherThan(current.getPriority()) || activity.getPriority() != current.getPriority()){
                    continue;
                }
            }
            activity.register(this);
            if (activity.canStart(Activity.StartType.START)){
                Profiler.ACTIVITIES.push("can start " + activity.getClass().getSimpleName());
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
            Profiler.ACTIVITIES.push("highest priority activity " + highest.getClass().getSimpleName());
            if (current != null) {
                if (highest.getPriority() == current.getPriority()) {
                    if (new Random().nextBoolean()){
                        Profiler.ACTIVITIES.push("select activity random " + highest.getClass().getSimpleName());
                        return highest;
                    }
                }
            } else {
                Profiler.ACTIVITIES.push("select activity " + highest.getClass().getSimpleName());
                return highest;
            }
        }
        return null;
    }
}
