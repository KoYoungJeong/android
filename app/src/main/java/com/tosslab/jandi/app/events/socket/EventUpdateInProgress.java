package com.tosslab.jandi.app.events.socket;

public class EventUpdateInProgress {
    private final int progress;
    private final int max;

    public EventUpdateInProgress(int progress, int max) {
        this.progress = progress;
        this.max = max;
    }

    public int getProgress() {
        return progress;
    }

    public int getMax() {
        return max;
    }
}
