package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 10. 6..
 */
public class ConfirmCreatePrivateTopicEvent {
    public String groupName;
    public ConfirmCreatePrivateTopicEvent(String groupName) {
        this.groupName = groupName;
    }
}
