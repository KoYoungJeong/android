package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
@DatabaseTable(tableName = "chat_rooms")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResChat {

    @DatabaseField
    private long teamId;
    @DatabaseField(id = true, columnName = "roomId")
    private int entityId;

    @DatabaseField
    private int lastMessageId;
    @DatabaseField
    private int unread;
    @DatabaseField(columnName = "userId")
    private int companionId;
    @DatabaseField
    private int lastLinkId;
    @DatabaseField
    private String lastMessage;
    @DatabaseField
    private String lastMessageStatus;

    public int getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getLastLinkId() {
        return lastLinkId;
    }

    public void setLastLinkId(int lastLinkId) {
        this.lastLinkId = lastLinkId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getCompanionId() {
        return companionId;
    }

    public ResChat companionId(int companionId) {
        this.companionId = companionId;
        return this;
    }

    public String getLastMessageStatus() {
        return lastMessageStatus;
    }

    public void setLastMessageStatus(String lastMessageStatus) {
        this.lastMessageStatus = lastMessageStatus;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }
}
