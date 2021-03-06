package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
public class MessageState {

    private long lastUpdateLinkId = -1;

    private long firstItemId = -1;

    private boolean isFirstMessage = false;

    private boolean isFirstLoadNewMessage = true;
    private boolean isFirstLoadOldMessage = true;
    private boolean loadHistory = true;

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

    public boolean isFirstLoadNewMessage() {
        return isFirstLoadNewMessage;
    }

    public void setIsFirstLoadNewMessage(boolean isFirstLoadNewMessage) {
        this.isFirstLoadNewMessage = isFirstLoadNewMessage;
    }

    public void setLoadHistory(boolean loadHistory) {
        this.loadHistory = loadHistory;
    }

    public boolean loadHistory() {
        return loadHistory;
    }

    public boolean isFirstLoadOldMessage() {
        return isFirstLoadOldMessage;
    }

    public void setIsFirstLoadOldMessage(boolean isFirstLoadOldMessage) {
        this.isFirstLoadOldMessage = isFirstLoadOldMessage;
    }
}
