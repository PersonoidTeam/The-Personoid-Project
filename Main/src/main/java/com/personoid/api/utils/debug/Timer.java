package com.personoid.api.utils.debug;

public class Timer {
    private long startTime;

    public Timer start() {
        startTime = System.currentTimeMillis();
        return this;
    }

    public long get() {
        return System.currentTimeMillis() - startTime;
    }
}
