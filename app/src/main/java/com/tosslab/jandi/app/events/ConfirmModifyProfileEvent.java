package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 8. 28..
 */
public class ConfirmModifyProfileEvent {
    public int actionType;
    public String inputMessage;
    public ConfirmModifyProfileEvent(int actionType, String inputMessage) {
        this.actionType = actionType;
        this.inputMessage = inputMessage;
    }
}
