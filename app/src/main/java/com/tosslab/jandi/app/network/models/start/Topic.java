package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Topic extends RealmObject {
    @PrimaryKey
    private long id;
    private long teamId;
    private String type;
    private String name;
    private String status;
    private String description;
    private boolean isDefault;
    private boolean autoJoin;
    private long creatorId;
    private long deleterId;
    private long lastLinkId;
    @Ignore
    private List<Long> members;
    private RealmList<RealmLong> memberIds;
    private RealmList<Marker> markers;
    private boolean isJoined;
    private boolean isStarred;
    private boolean subscribe;
    private Announcement announcement;
    private long readLinkId;
    private int unreadCount;
    private boolean isAnnouncement;

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

    @Deprecated
    public List<Long> getMembers() {
        if (members != null) {
            return members;
        } else {
            members = new ArrayList<>();
            if (memberIds != null) {
                for (RealmLong memberId : memberIds) {
                    members.add(memberId.getValue());
                }
            }
            return members;
        }
    }

    @Deprecated
    public void setMembers(List<Long> members) {
        this.members = members;
    }

    public RealmList<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(RealmList<Marker> markers) {
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

    public RealmList<RealmLong> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(RealmList<RealmLong> memberIds) {
        this.memberIds = memberIds;
    }

}
