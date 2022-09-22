package com.personoid.api.ai.activity;

import com.personoid.api.npc.NPC;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.Priority;

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
                Profiler.ACTIVITIES.push("Pausing " + current.getClass().getSimpleName() + " to start " + queue.peek().getClass().getSimpleName());
                current.internalStop(Activity.StopType.PAUSE);
                current.onStop(Activity.StopType.PAUSE);
                current.setPaused(true);
                paused.add(current);
                current = null;
            } else {
                queueIfEmpty();
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
                    Profiler.ACTIVITIES.push("Restarted paused activity");
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

    public Set<Activity> getRegisteredActivities() {
        return new HashSet<>(registered);
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
            Profiler.ACTIVITIES.push("Choosing new activity to queue");
            Activity chosen = chooseViaPriority();
            if (chosen != null) {
                queueActivity(chosen);
                Profiler.ACTIVITIES.push("Queued activity: " + chosen.getClass().getSimpleName());
            }
        } else {
            StringBuilder queueString = new StringBuilder();
            for (int i = 0; i < queue.size(); i++) {
                queueString.append(queue.peek().getClass().getSimpleName());
                if (i < queue.size() - 1) {
                    queueString.append(", ");
                }
            }
            Profiler.ACTIVITIES.push("Activities already in queue: " + queueString);
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
        // get all activities that can start.
        for (Activity activity : registered) {
            Profiler.ACTIVITIES.push("Looping through activity: " + activity.getClass().getSimpleName());
            if (boredTasks.containsKey(activity)) continue;
            if (current != null) {
                if (!activity.getPriority().isHigherThan(current.getPriority())) {
                    continue;
                }
            }
            activity.register(this);
            if (activity.canStart(Activity.StartType.START)) {
                Profiler.ACTIVITIES.push("Can start " + activity.getClass().getSimpleName());
                canStart.add(activity);
            }
        }
        Priority highestPriority = Priority.LOWEST;
        List<Activity> highest = new ArrayList<>();
        for (Activity activity : canStart) {
            if (activity.getPriority().equals(highestPriority)) {
                highest.add(activity);
            }
            if (activity.getPriority().isHigherThan(highestPriority)) {
                highest.clear();
                highestPriority = activity.getPriority();
                highest.add(activity);
            }
        }
        if (!highest.isEmpty()) {
            Profiler.ACTIVITIES.push("All possible activities count: "+highest.size());
            for (Activity activity : highest){
                Profiler.ACTIVITIES.push("Potential Activity: "+activity.getClass().getSimpleName());
            }
            if (highest.size() > 1) {
                Activity chosen = highest.get(MathUtils.random(0, highest.size() - 1));
                Profiler.ACTIVITIES.push("Selected random activity: " + chosen.getClass().getSimpleName());
                return chosen;
            }
            else {
                Profiler.ACTIVITIES.push("Selected activity: " + highest.get(0).getClass().getSimpleName());
                return highest.get(0);
            }
        }
        return null;
    }
}
