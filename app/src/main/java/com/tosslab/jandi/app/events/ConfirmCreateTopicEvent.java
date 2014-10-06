package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 10. 6..
 */
public class ConfirmCreateTopicEvent {
    public String topicName;
    public ConfirmCreateTopicEvent(String topicName) {
        this.topicName = topicName;
    }
}
