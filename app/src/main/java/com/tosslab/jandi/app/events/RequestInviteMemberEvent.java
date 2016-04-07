package com.tosslab.jandi.app.events;

/**
 * Created by tonyjs on 15. 5. 6..
 */
public class RequestInviteMemberEvent {
    private int from;

    public RequestInviteMemberEvent(int from) {
        this.from = from;
    }

    public RequestInviteMemberEvent() {
    }

    public int getFrom() {
        return from;
    }
}
