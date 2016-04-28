package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

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
    private long entityId;

    @DatabaseField
    private long lastMessageId;
    @DatabaseField
    private int unread;
    @DatabaseField(columnName = "userId")
    private long companionId;
    @DatabaseField
    private long lastLinkId;
    @DatabaseField
    private String lastMessage;
    @DatabaseField
    private String lastMessageStatus;
    @DatabaseField
    private int order;
    @DatabaseField
    private boolean isOld;

    public long getLastMessageId() {
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

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public long getLastLinkId() {
        return lastLinkId;
    }

    public void setLastLinkId(long lastLinkId) {
        this.lastLinkId = lastLinkId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getCompanionId() {
        return companionId;
    }

    public ResChat companionId(long companionId) {
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isOld() {
        return isOld;
    }

    public void setIsOld(boolean isOld) {
        this.isOld = isOld;
    }
}
