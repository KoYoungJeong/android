package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by tonyjs on 15. 6. 17..
 */
public class UpdateMessage {
    private int teamId;
    private int messageId;

    public UpdateMessage(int teamId, int messageId) {
        this.teamId = teamId;
        this.messageId = messageId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
