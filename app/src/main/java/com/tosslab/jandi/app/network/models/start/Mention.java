package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vimeo.stag.GsonAdapterKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Mention {
    @GsonAdapterKey
    int unreadCount;
    @GsonAdapterKey
    long lastMentionedMessageId;

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
}
