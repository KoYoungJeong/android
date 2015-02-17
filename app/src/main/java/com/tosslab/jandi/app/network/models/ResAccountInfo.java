package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResAccountInfo {
    private String id;
    private String name;
    private String tutoredAt;
    private String updatedAt;
    private String createdAt;
    private String loggedAt;
    private String activatedAt;
    private String notificationTarget;
    private String status;

    private List<UserDevice> devices;
    private List<UserTeam> memberships;
    private List<UserEmail> emails;

    @JsonProperty("u_photoThumbnailUrl")
    private ResMessages.ThumbnailUrls photoThumbnailUrl;
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

    public List<UserDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<UserDevice> devices) {
        this.devices = devices;
    }

    public List<UserTeam> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<UserTeam> memberships) {
        this.memberships = memberships;
    }

    public List<UserEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<UserEmail> emails) {
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

    public ResMessages.ThumbnailUrls getPhotoThumbnailUrl() {
        return photoThumbnailUrl;
    }

    public void setPhotoThumbnailUrl(ResMessages.ThumbnailUrls photoThumbnailUrl) {
        this.photoThumbnailUrl = photoThumbnailUrl;
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

    public static class UserDevice {
        private String token;
        private String type;
        private int badgeCount;
        private boolean subscribe;

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
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class UserTeam {
        private int teamId;
        private int memberId;
        private String name;

        @JsonProperty("t_domain")
        private String teamDomain;
        private int unread;
        private String status;

        public int getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class UserEmail {

        private String id;
        private boolean primary;
        private String confirmedAt;
        private String status;

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
}
