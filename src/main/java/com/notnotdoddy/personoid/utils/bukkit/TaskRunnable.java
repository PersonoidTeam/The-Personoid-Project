package com.notnotdoddy.personoid.utils.bukkit;

public interface TaskRunnable extends Runnable {
    Task task = null;

    default void cancel() {
        task.cancel();
    }

    default void onCancel() {};

    default void onComplete() {};

    default int getIteration() {
        return task.getCurrentTask().currentLoop;
    }
}
