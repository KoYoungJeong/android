package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.TopicDaoImpl;
import com.tosslab.jandi.app.local.orm.persister.CollectionLongConverter;
import com.tosslab.jandi.app.local.orm.persister.DateConverter;

import java.util.Collection;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_topic", daoClass = TopicDaoImpl.class)
public class Topic {
    @JsonIgnore
    @DatabaseField(foreign = true)
    InitializeInfo initialInfo;
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String type;
    @DatabaseField
    private String name;
    @DatabaseField
    private String status;
    @DatabaseField
    private String description;
    @DatabaseField
    private boolean isDefault;
    @DatabaseField
    private boolean autoJoin;
    @DatabaseField
    private long creatorId;
    @DatabaseField
    private long deleterId;
    @DatabaseField
    private long lastLinkId;
    @DatabaseField(persisterClass = CollectionLongConverter.class)
    private Collection<Long> members;
    @ForeignCollectionField(foreignFieldName = "topic")
    private Collection<Marker> markers;
    @DatabaseField
    private boolean isJoined;
    @DatabaseField
    private boolean isStarred;
    @DatabaseField
    private boolean subscribe;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Announcement announcement;
    @DatabaseField
    private long readLinkId;
    @DatabaseField
    private int unreadCount;

    public InitializeInfo getInitialInfo() {
        return initialInfo;
    }

    public void setInitialInfo(InitializeInfo initialInfo) {
        this.initialInfo = initialInfo;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_info_topic_announce")
    public static class Announcement {
        @DatabaseField(id = true)
        private long messageId;
        @DatabaseField
        private String content;
        @DatabaseField
        private long writerId;
        @DatabaseField
        private long creatorId;
        @DatabaseField(persisterClass = DateConverter.class)
        private Date writtenAt;
        @DatabaseField(persisterClass = DateConverter.class)
        private Date createdAt;
        @DatabaseField
        private boolean isOpened;

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getWriterId() {
            return writerId;
        }

        public void setWriterId(long writerId) {
            this.writerId = writerId;
        }

        public long getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(long creatorId) {
            this.creatorId = creatorId;
        }

        public Date getWrittenAt() {
            return writtenAt;
        }

        public void setWrittenAt(Date writtenAt) {
            this.writtenAt = writtenAt;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public boolean isOpened() {
            return isOpened;
        }

        public void setIsOpened(boolean opened) {
            isOpened = opened;
        }

    }
}
