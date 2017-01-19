package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.jackson.deserialize.start.ChatConverter;
import com.vimeo.stag.GsonAdapterKey;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(converter = ChatConverter.class)
public class Chat {
    @GsonAdapterKey
    long id;
    @GsonAdapterKey
    long teamId;
    @GsonAdapterKey
    String type;
    @GsonAdapterKey
    String status;
    @GsonAdapterKey
    long lastLinkId;
    @GsonAdapterKey
    List<Long> members;
    @GsonAdapterKey
    boolean isOpened;
    @GsonAdapterKey
    long companionId;
    @GsonAdapterKey
    LastMessage lastMessage;
    @GsonAdapterKey
    List<Marker> markers;
    @GsonAdapterKey
    long readLinkId;
    @GsonAdapterKey
    int unreadCount;

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

    public List<Long> getMembers() {
        return members;
    }

    public void setMembers(List<Long> members) {
        this.members = members;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
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

    public long getCompanionId() {
        return companionId;
    }

    public void setCompanionId(long companionId) {
        this.companionId = companionId;
    }

}
