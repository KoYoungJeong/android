package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MessageState {

    private int lastUpdateLinkId = -1;

    private int firstItemId = -1;
    private boolean isFirstMessage = false;

    public int getLastUpdateLinkId() {
        return lastUpdateLinkId;
    }

    public void setLastUpdateLinkId(int lastUpdateLinkId) {
        this.lastUpdateLinkId = lastUpdateLinkId;
    }

    public int getFirstItemId() {
        return firstItemId;
    }

    public void setFirstItemId(int firstItemId) {
        this.firstItemId = firstItemId;
    }

    public boolean isFirstMessage() {
        return isFirstMessage;
    }

    public void setFirstMessage(boolean isFirstMessage) {
        this.isFirstMessage = isFirstMessage;
    }
}