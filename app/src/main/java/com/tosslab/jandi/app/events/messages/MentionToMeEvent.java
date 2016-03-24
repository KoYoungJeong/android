package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 16. 3. 24..
 */
public class MentionToMeEvent {
    private long teamId;

    public MentionToMeEvent(long teamId) {
        this.teamId = teamId;
    }

    public long getTeamId() {
        return teamId;
    }
}
