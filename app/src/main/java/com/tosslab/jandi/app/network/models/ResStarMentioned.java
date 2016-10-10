package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;

import java.util.List;

/**
 * Created by tee on 15. 7. 29..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResStarMentioned {

    private boolean hasMore;
    private boolean isLimited;
    private int unreadCount;
    private long lastMentionedMessageId;

    private List<StarredMessage> records;

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<StarredMessage> getRecords() {
        return records;
    }

    public void setRecords(List<StarredMessage> records) {
        this.records = records;
    }

    public boolean isLimited() {
        return isLimited;
    }

    public void setLimited(boolean limited) {
        isLimited = limited;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getLastMentionedMessageId() {
        return lastMentionedMessageId;
    }

    public void setLastMentionedMessageId(long lastMentionedMessageId) {
        this.lastMentionedMessageId = lastMentionedMessageId;
    }

    @Override
    public String toString() {
        return "ResMentioned{" +
                "hasMore=" + hasMore +
                ", records=" + records.toString() +
                '}';
    }
}
