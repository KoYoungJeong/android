package com.tosslab.jandi.app.ui.maintab.more.domain;

public class VersionClickedInfo {
    private long time;
    private int count;

    public VersionClickedInfo() {
        time = 0L;
        count = 0;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
