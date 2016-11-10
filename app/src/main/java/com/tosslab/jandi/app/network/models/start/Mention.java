package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Mention extends RealmObject {
    @JsonIgnore
    @PrimaryKey
    private long id;

    private int unreadCount;
    private long lastMentionedMessageId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
