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

    public String getName() {
        return name;
    }

    public String getTutoredAt() {
        return tutoredAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLoggedAt() {
        return loggedAt;
    }

    public String getActivatedAt() {
        return activatedAt;
    }

    public String getNotificationTarget() {
        return notificationTarget;
    }

    public String getStatus() {
        return status;
    }

    public List<UserDevice> getDevices() {
        return devices;
    }

    public List<UserTeam> getMemberships() {
        return memberships;
    }

    public List<UserEmail> getEmails() {
        return emails;
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

    public static class UserDevice {
        private String token;
        private String type;
        private int badgeCount;
        private boolean subscribe;

        public String getToken() {
            return token;
        }

        public String getType() {
            return type;
        }

        public int getBadgeCount() {
            return badgeCount;
        }

        public boolean isSubscribe() {
            return subscribe;
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
    private static class UserTeam {
        private int teamId;
        private int memberId;
        private String name;

        @JsonProperty("t_domain")
        private String teamDomain;
        private int unread;

        public int getTeamId() {
            return teamId;
        }

        public int getMemberId() {
            return memberId;
        }

        public String getName() {
            return name;
        }

        public String getTeamDomain() {
            return teamDomain;
        }

        public int getUnread() {
            return unread;
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

        private String email;
        private boolean primary;
        private String confirmedAt;
        private String status;

        public String getEmail() {
            return email;
        }

        public boolean isPrimary() {
            return primary;
        }

        public String getConfirmedAt() {
            return confirmedAt;
        }

        public String getStatus() {
            return status;
        }

        @Override
        public String toString() {
            return "UserEmail{" +
                    "email='" + email + '\'' +
                    ", primary=" + primary +
                    ", confirmedAt='" + confirmedAt + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
