package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.ChatDaoImpl;
import com.tosslab.jandi.app.local.orm.persister.CollectionLongConverter;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_chat", daoClass = ChatDaoImpl.class)
public class Chat {
    @JsonIgnore
    @DatabaseField(foreign = true)
    private InitialInfo initialInfo;
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String type;
    @DatabaseField
    private String status;
    @DatabaseField
    private long lastLinkId;
    @DatabaseField(persisterClass = CollectionLongConverter.class)
    private Collection<Long> members;
    @ForeignCollectionField(foreignFieldName = "chat")
    private Collection<Marker> markers;
    @DatabaseField
    private long companionId;
    @DatabaseField
    private boolean isOpened;
    @DatabaseField
    private long readLinkId;
    @DatabaseField
    private int unreadCount;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LastMessage lastMessage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastLinkId() {
        return lastLinkId;
    }

    public void setLastLinkId(long lastLinkId) {
        this.lastLinkId = lastLinkId;
    }

    public Collection<Long> getMembers() {
        return members;
    }

    public void setMembers(Collection<Long> members) {
        this.members = members;
    }

    public Collection<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(Collection<Marker> markers) {
        this.markers = markers;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setIsOpened(boolean opened) {
        isOpened = opened;
    }

    public long getReadLinkId() {
        return readLinkId;
    }

    public void setReadLinkId(long readLinkId) {
        this.readLinkId = readLinkId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LastMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public InitialInfo getInitialInfo() {
        return initialInfo;
    }

    public void setInitialInfo(InitialInfo initialInfo) {
        this.initialInfo = initialInfo;
    }

    public long getCompanionId() {
        return companionId;
    }

    public void setCompanionId(long companionId) {
        this.companionId = companionId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_info_chat_lastmessage")
    public static class LastMessage {
        @DatabaseField(id = true)
        private long id;
        @DatabaseField
        private String text;
        @DatabaseField
        private String status;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
