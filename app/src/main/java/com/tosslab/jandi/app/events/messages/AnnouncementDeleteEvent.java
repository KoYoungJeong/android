package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 15. 6. 24..
 */
public class AnnouncementDeleteEvent {

    private long topicId;

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }
}
