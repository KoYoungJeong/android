package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.jackson.deserialize.start.TopicConverter;
import com.vimeo.stag.GsonAdapterKey;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(converter = TopicConverter.class)
public class Topic {
    @GsonAdapterKey
    long id;
    @GsonAdapterKey
    long teamId;
    @GsonAdapterKey
    String type;
    @GsonAdapterKey
    String name;
    @GsonAdapterKey
    String status;
    @GsonAdapterKey
    String description;
    @GsonAdapterKey
    boolean isDefault;
    @GsonAdapterKey
    boolean autoJoin;
    @GsonAdapterKey
    boolean isAnnouncement;
    @GsonAdapterKey
    boolean messageDeletable;
    @GsonAdapterKey
    long creatorId;
    @GsonAdapterKey
    long deleterId;
    @GsonAdapterKey
    long lastLinkId;
    @GsonAdapterKey
    Date createdAt;
    @GsonAdapterKey
    List<Long> members;
    @GsonAdapterKey
    boolean isJoined;
    @GsonAdapterKey
    boolean isStarred;
    @GsonAdapterKey
    List<Marker> markers;
    @GsonAdapterKey
    long readLinkId;
    @GsonAdapterKey
    boolean subscribe;
    @GsonAdapterKey
    int unreadCount;
    @GsonAdapterKey
    Announcement announcement;

    public boolean isAnnouncement() {
        return isAnnouncement;
    }

    public void setIsAnnouncement(boolean announcement) {
        isAnnouncement = announcement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public long getDeleterId() {
        return deleterId;
    }

    public void setDeleterId(long deleterId) {
        this.deleterId = deleterId;
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

    public boolean isJoined() {
        return isJoined;
    }

    public void setIsJoined(boolean joined) {
        isJoined = joined;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setIsStarred(boolean starred) {
        isStarred = starred;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
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


    public boolean isMessageDeletable() {
        return messageDeletable;
    }

    public void setMessageDeletable(boolean messageDeletable) {
        this.messageDeletable = messageDeletable;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
