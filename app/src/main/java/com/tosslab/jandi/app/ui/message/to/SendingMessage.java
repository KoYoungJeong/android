package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class SendingMessage {
    private final long localId;
    private final String message;
    private int messageId;

    public SendingMessage(long localId, String message) {
        this.localId = localId;
        this.message = message;
    }

    public long getLocalId() {
        return localId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

}
