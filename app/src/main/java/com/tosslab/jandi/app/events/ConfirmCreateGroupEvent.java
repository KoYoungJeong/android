package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 10. 6..
 */
public class ConfirmCreateGroupEvent {
    public String groupName;
    public ConfirmCreateGroupEvent(String groupName) {
        this.groupName = groupName;
    }
}
