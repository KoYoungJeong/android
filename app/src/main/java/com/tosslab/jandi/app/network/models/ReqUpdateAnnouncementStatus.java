package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 15. 6. 24..
 */
public class ReqUpdateAnnouncementStatus {
    private long topicId;
    private boolean opened;

    public ReqUpdateAnnouncementStatus(long topicId, boolean opened) {
        this.topicId = topicId;
        this.opened = opened;
    }

    public long getTopicId() {
        return topicId;
    }

    public boolean isOpened() {
        return opened;
    }
}
