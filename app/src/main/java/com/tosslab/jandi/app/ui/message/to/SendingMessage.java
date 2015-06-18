package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class SendingMessage {
    private final long localId;
    private final String message;
    private int messageId;
    private StickerInfo stickerInfo;

    public SendingMessage(long localId, String message) {
        this.localId = localId;
        this.message = message;
    }

    public SendingMessage(long localId, String message, StickerInfo stickerInfo) {
        this.localId = localId;
        this.message = message;
        this.stickerInfo = stickerInfo;
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

    public StickerInfo getStickerInfo() {
        return stickerInfo;
    }

    public void setStickerInfo(StickerInfo stickerInfo) {
        this.stickerInfo = stickerInfo;
    }
}
