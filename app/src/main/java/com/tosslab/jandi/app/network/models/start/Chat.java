package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Chat extends RealmObject {
    @PrimaryKey
    private long id;
    private long teamId;
    private String type;
    private String status;
    private long lastLinkId;
    @Ignore
    private List<Long> members;
    private RealmList<RealmLong> memberIds;
    private RealmList<Marker> markers;
    private long companionId;
    private boolean isOpened;
    private long readLinkId;
    private int unreadCount;
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

    public List<Long> getMembers() {
        return members;
    }

    public void setMembers(List<Long> members) {
        this.members = members;
    }

    public RealmList<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(RealmList<Marker> markers) {
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

    public RealmList<RealmLong> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(RealmList<RealmLong> memberIds) {
        this.memberIds = memberIds;
    }

}
