package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 15. 6. 24..
 */
public class AnnouncementUpdatedEvent {

    private long topicId;
    private boolean isOpened;

    public AnnouncementUpdatedEvent(long topicId, boolean isOpened) {
        this.topicId = topicId;
        this.isOpened = isOpened;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }
}
