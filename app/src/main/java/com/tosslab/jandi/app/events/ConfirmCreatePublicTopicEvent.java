package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 10. 6..
 */
public class ConfirmCreatePublicTopicEvent {
    public String topicName;
    public ConfirmCreatePublicTopicEvent(String topicName) {
        this.topicName = topicName;
    }
}
