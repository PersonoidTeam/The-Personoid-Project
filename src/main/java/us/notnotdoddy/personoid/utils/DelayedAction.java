package us.notnotdoddy.personoid.utils;

import me.definedoddy.fluidapi.FluidPlugin;
import org.bukkit.Bukkit;
import us.notnotdoddy.personoid.npc.NPCHandler;
import us.notnotdoddy.personoid.npc.PersonoidNPC;

public abstract class DelayedAction {
    private final int taskId;
    private final long delay;
    private boolean async;

    public DelayedAction(PersonoidNPC npc, long delay) {
        this.delay = delay;
        this.taskId = Bukkit.getScheduler().runTaskLater(FluidPlugin.getPlugin(), this::run, delay).getTaskId();
    }

    public DelayedAction(PersonoidNPC npc, long delay, boolean async) {
        this.delay = delay;
        this.async = async;
        if (async) {
            this.taskId = Bukkit.getScheduler().runTaskLaterAsynchronously(FluidPlugin.getPlugin(), () -> {
                if (NPCHandler.getNPCs().containsValue(npc)) {
                    this.run();
                    this.onComplete();
                }
            }, delay).getTaskId();
        } else {
            this.taskId = Bukkit.getScheduler().runTaskLater(FluidPlugin.getPlugin(), () -> {
                if (NPCHandler.getNPCs().containsValue(npc)) {
                    this.run();
                    this.onComplete();
                }
            }, delay).getTaskId();
        }

    }

    public abstract void run();

    public void onComplete() {
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.taskId);
    }

    public int getId() {
        return this.taskId;
    }
}
