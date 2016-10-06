package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.AccountInfoDaoImpl;

import java.util.Collection;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
@DatabaseTable(tableName = "accounts", daoClass = AccountInfoDaoImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResAccountInfo {

    @DatabaseField(id = true)
    private String id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String tutoredAt;
    @DatabaseField
    private String updatedAt;
    @DatabaseField
    private String createdAt;
    @DatabaseField
    private String loggedAt;
    @DatabaseField
    private String activatedAt;
    @DatabaseField
    private String notificationTarget;
    @DatabaseField
    private String status;

    @ForeignCollectionField()
    private Collection<UserDevice> devices;
    @ForeignCollectionField()
    private Collection<UserTeam> memberships;
    @ForeignCollectionField()
    private Collection<UserEmail> emails;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true)
    @JsonProperty("u_photoThumbnailUrl")
    private ThumbnailInfo thumbnailInfo;

    @DatabaseField
    @JsonProperty("u_photoUrl")
    private String photoUrl;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTutoredAt() {
        return tutoredAt;
    }

    public void setTutoredAt(String tutoredAt) {
        this.tutoredAt = tutoredAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(String loggedAt) {
        this.loggedAt = loggedAt;
    }

    public String getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(String activatedAt) {
        this.activatedAt = activatedAt;
    }

    public String getNotificationTarget() {
        return notificationTarget;
    }

    public void setNotificationTarget(String notificationTarget) {
        this.notificationTarget = notificationTarget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Collection<UserDevice> getDevices() {
        return devices;
    }

    public void setDevices(Collection<UserDevice> devices) {
        this.devices = devices;
    }

    public Collection<UserTeam> getMemberships() {
        return memberships;
    }

    public void setMemberships(Collection<UserTeam> memberships) {
        this.memberships = memberships;
    }

    public Collection<UserEmail> getEmails() {
        return emails;
    }

    public void setEmails(Collection<UserEmail> emails) {
        this.emails = emails;
    }

    @Override
    public String toString() {
        return "ResAccountInfo{" +
                "name='" + name + '\'' +
                ", tutoredAt='" + tutoredAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", loggedAt='" + loggedAt + '\'' +
                ", activatedAt='" + activatedAt + '\'' +
                ", notificationTarget='" + notificationTarget + '\'' +
                ", status='" + status + '\'' +
                ", devices=" + devices +
                ", memberships=" + memberships +
                ", emails=" + emails +
                '}';
    }

    public ThumbnailInfo getThumbnailInfo() {
        return thumbnailInfo;
    }

    public void setThumbnailInfo(ThumbnailInfo thumbnailInfo) {
        this.thumbnailInfo = thumbnailInfo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DatabaseTable(tableName = "account_devices")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class UserDevice {

        @DatabaseField(foreign = true)
        @JsonIgnore
        ResAccountInfo accountInfo;
        @DatabaseField(generatedId = true, readOnly = true)
        @JsonIgnore
        private long _id;
        @DatabaseField
        private String token;
        @DatabaseField
        private String type;
        @DatabaseField
        private int badgeCount;
        @DatabaseField
        private boolean subscribe;

        public ResAccountInfo getAccountInfo() {
            return accountInfo;
        }

        public void setAccountInfo(ResAccountInfo accountInfo) {
            this.accountInfo = accountInfo;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getBadgeCount() {
            return badgeCount;
        }

        public void setBadgeCount(int badgeCount) {
            this.badgeCount = badgeCount;
        }

        public boolean isSubscribe() {
            return subscribe;
        }

        public void setSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
        }

        @Override
        public String toString() {
            return "UserDevice{" +
                    "token='" + token + '\'' +
                    ", type='" + type + '\'' +
                    ", badgeCount=" + badgeCount +
                    ", subscribe=" + subscribe +
                    '}';
        }

        public long get_id() {
            return _id;
        }

        public void set_id(long _id) {
            this._id = _id;
        }
    }


    @DatabaseTable(tableName = "account_teams")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class UserTeam {
        @DatabaseField(foreign = true)
        @JsonIgnore
        ResAccountInfo accountInfo;
        @DatabaseField(id = true)
        private long teamId;
        @DatabaseField
        private long memberId;
        @DatabaseField
        private String email;
        @DatabaseField
        private String name;
        @DatabaseField
        @JsonProperty("t_domain")
        private String teamDomain;
        @DatabaseField
        private int unread;
        @DatabaseField
        private String status;

        @DatabaseField
        private int order;

        public ResAccountInfo getAccountInfo() {
            return accountInfo;
        }

        public void setAccountInfo(ResAccountInfo accountInfo) {
            this.accountInfo = accountInfo;
        }

        public long getTeamId() {
            return teamId;
        }

        public void setTeamId(long teamId) {
            this.teamId = teamId;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTeamDomain() {
            return teamDomain;
        }

        public void setTeamDomain(String teamDomain) {
            this.teamDomain = teamDomain;
        }

        public int getUnread() {
            return unread;
        }

        public void setUnread(int unread) {
            this.unread = unread;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "UserTeam{" +
                    "teamId=" + teamId +
                    ", memberId=" + memberId +
                    ", name='" + name + '\'' +
                    ", teamDomain='" + teamDomain + '\'' +
                    ", unread=" + unread +
                    '}';
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }

    @DatabaseTable(tableName = "account_emails")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class UserEmail {
        @DatabaseField(foreign = true)
        @JsonIgnore
        ResAccountInfo accountInfo;
        @DatabaseField(id = true)
        private String id;
        @DatabaseField
        private boolean primary;
        @DatabaseField
        private String confirmedAt;
        @DatabaseField
        private String status;

        public ResAccountInfo getAccountInfo() {
            return accountInfo;
        }

        public void setAccountInfo(ResAccountInfo accountInfo) {
            this.accountInfo = accountInfo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public String getConfirmedAt() {
            return confirmedAt;
        }

        public void setConfirmedAt(String confirmedAt) {
            this.confirmedAt = confirmedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "UserEmail{" +
                    "id='" + id + '\'' +
                    ", is_primary=" + primary +
                    ", confirmedAt='" + confirmedAt + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "account_thumbnail")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbnailInfo {

        @DatabaseField(generatedId = true)
        @JsonIgnore
        private long _id;

        @DatabaseField
        private String smallThumbnailUrl;
        @DatabaseField
        private String mediumThumbnailUrl;
        @DatabaseField
        private String largeThumbnailUrl;

        @Override
        public String toString() {
            return "ThumbnailUrls{" +
                    "smallThumbnailUrl='" + getSmallThumbnailUrl() + '\'' +
                    ", mediumThumbnailUrl='" + getMediumThumbnailUrl() + '\'' +
                    ", largeThumbnailUrl='" + getLargeThumbnailUrl() + '\'' +
                    '}';
        }

        public String getSmallThumbnailUrl() {
            return smallThumbnailUrl;
        }

        public void setSmallThumbnailUrl(String smallThumbnailUrl) {
            this.smallThumbnailUrl = smallThumbnailUrl;
        }

        public String getMediumThumbnailUrl() {
            return mediumThumbnailUrl;
        }

        public void setMediumThumbnailUrl(String mediumThumbnailUrl) {
            this.mediumThumbnailUrl = mediumThumbnailUrl;
        }

        public String getLargeThumbnailUrl() {
            return largeThumbnailUrl;
        }

        public void setLargeThumbnailUrl(String largeThumbnailUrl) {
            this.largeThumbnailUrl = largeThumbnailUrl;
        }

        public long get_id() {
            return _id;
        }

        public void set_id(long _id) {
            this._id = _id;
        }

    }
}
