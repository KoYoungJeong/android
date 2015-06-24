package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 15. 6. 24..
 */
public class ReqUpdateAnnouncementStatus {
    private int topicId;
    private boolean opened;

    public ReqUpdateAnnouncementStatus(int topicId, boolean opened) {
        this.topicId = topicId;
        this.opened = opened;
    }

    public int getTopicId() {
        return topicId;
    }

    public boolean isOpened() {
        return opened;
    }
}
