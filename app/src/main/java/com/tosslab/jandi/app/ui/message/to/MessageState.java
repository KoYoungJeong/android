package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MessageState {

    private long lastUpdateLinkId = -1;

    private long firstItemId = -1;

    private boolean isFirstMessage = false;

    public long getLastUpdateLinkId() {
        return lastUpdateLinkId;
    }

    public void setLastUpdateLinkId(long lastUpdateLinkId) {
        this.lastUpdateLinkId = lastUpdateLinkId;
    }

    public long getFirstItemId() {
        return firstItemId;
    }

    public void setFirstItemId(long firstItemId) {
        this.firstItemId = firstItemId;
    }

    public boolean isFirstMessage() {
        return isFirstMessage;
    }

    public void setIsFirstMessage(boolean isFirstMessage) {
        this.isFirstMessage = isFirstMessage;
    }

}
