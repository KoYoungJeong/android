package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResTeamDetailInfo {

    @JsonProperty("team")
    private InviteTeam inviteTeam;
    @JsonProperty("member")
    private InviteTeamMember inviteTeamMember;

    public InviteTeam getInviteTeam() {
        return inviteTeam;
    }

    public InviteTeamMember getInviteTeamMember() {
        return inviteTeamMember;
    }

    @Override
    public String toString() {
        return "ResInvitationConfirm{" +
                "inviteTeam=" + inviteTeam +
                ", inviteTeamMember=" + inviteTeamMember +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class InviteTeam {
        private String _Id;
        private long teamId;
        private long id;
        @JsonProperty("t_emailDomain")
        private String teamEmailDomain;
        @JsonProperty("t_domain")
        private String teamDomain;
        private String name;
        private String updatedAt;
        private String createdAt;
        @JsonProperty("t_defaultChannelId")
        private String teamDefaultChannelId;
        @JsonProperty("t_members")
        private List<Long> teamMembers;
        private String status;
        private String type;
        private String invitationStatus;
        private String invitationCode;
        private String invitationUrl;

        public String get_Id() {
            return _Id;
        }

        public long getTeamId() {
            return teamId;
        }

        public long getId() {
            return id;
        }

        public String getTeamEmailDomain() {
            return teamEmailDomain;
        }

        public String getTeamDomain() {
            return teamDomain;
        }

        public String getName() {
            return name;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getTeamDefaultChannelId() {
            return teamDefaultChannelId;
        }

        public List<Long> getTeamMembers() {
            return teamMembers;
        }

        public String getStatus() {
            return status;
        }

        public String getType() {
            return type;
        }

        public String getInvitationStatus() {
            return invitationStatus;
        }

        public void setInvitationStatus(String invitationStatus) {
            this.invitationStatus = invitationStatus;
        }

        public String getInvitationCode() {
            return invitationCode;
        }

        public void setInvitationCode(String invitationCode) {
            this.invitationCode = invitationCode;
        }

        public String getInvitationUrl() {
            return invitationUrl;
        }

        public void setInvitationUrl(String invitationUrl) {
            this.invitationUrl = invitationUrl;
        }

        @Override
        public String toString() {
            return "InviteTeam{" +
                    "_Id='" + _Id + '\'' +
                    ", teamId=" + teamId +
                    ", id=" + id +
                    ", teamEmailDomain='" + teamEmailDomain + '\'' +
                    ", teamDomain='" + teamDomain + '\'' +
                    ", name='" + name + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", teamDefaultChannelId='" + teamDefaultChannelId + '\'' +
                    ", teamMembers=" + teamMembers +
                    ", status='" + status + '\'' +
                    ", type='" + type + '\'' +
                    ", invitationStatus='" + invitationStatus + '\'' +
                    ", invitationCode='" + invitationCode + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class InviteTeamMember {
        private long id;
        private String _id;
        private String accountId;
        @JsonProperty("u_authority")
        private String userAuthority;
        @JsonProperty("u_email")
        private String userEmail;
        private String name;
        private String teamId;
        private String updatedAt;
        private String createdAt;
        @JsonProperty("u_statusMessage")
        private String userStatusMessage;
        @JsonProperty("u_messageMarkers")
        private List<String> userMessageMarkers;
        @JsonProperty("u_joinEntities")
        private List<String> userJoinEntities;
        @JsonProperty("u_starredMessages")
        private List<String> userStarredMessages;
        @JsonProperty("u_starredEntities")
        private List<String> userstarredEntities;
        @JsonProperty("u_extraData")
        private ExtraData userExtraData;
        @JsonProperty("u_photoThumbnailUrl")
        private ResMessages.ThumbnailUrls userPhotoThumbnailUrl;
        @JsonProperty("u_photoUrl")
        private String userPhotoUrl;
        private String status;
        private String type;

        public long getId() {
            return id;
        }

        public String get_id() {
            return _id;
        }

        public String getAccountId() {
            return accountId;
        }

        public String getUserAuthority() {
            return userAuthority;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public String getName() {
            return name;
        }

        public String getTeamId() {
            return teamId;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUserStatusMessage() {
            return userStatusMessage;
        }

        public List<String> getUserMessageMarkers() {
            return userMessageMarkers;
        }

        public List<String> getUserJoinEntities() {
            return userJoinEntities;
        }

        public List<String> getUserStarredMessages() {
            return userStarredMessages;
        }

        public List<String> getUserstarredEntities() {
            return userstarredEntities;
        }

        public ExtraData getUserExtraData() {
            return userExtraData;
        }

        public ResMessages.ThumbnailUrls getUserPhotoThumbnailUrl() {
            return userPhotoThumbnailUrl;
        }

        public String getUserPhotoUrl() {
            return userPhotoUrl;
        }

        public String getStatus() {
            return status;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "InviteTeamMember{" +
                    "id=" + id +
                    ", _id='" + _id + '\'' +
                    ", accountId='" + accountId + '\'' +
                    ", userAuthority='" + userAuthority + '\'' +
                    ", userEmail='" + userEmail + '\'' +
                    ", name='" + name + '\'' +
                    ", teamId='" + teamId + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", userStatusMessage='" + userStatusMessage + '\'' +
                    ", userMessageMarkers='" + userMessageMarkers + '\'' +
                    ", userJoinEntities='" + userJoinEntities + '\'' +
                    ", userStarredMessages='" + userStarredMessages + '\'' +
                    ", userstarredEntities='" + userstarredEntities + '\'' +
                    ", userExtraData=" + userExtraData +
                    ", userPhotoThumbnailUrl='" + userPhotoThumbnailUrl + '\'' +
                    ", userPhotoUrl='" + userPhotoUrl + '\'' +
                    ", status='" + status + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private static class ExtraData {
        private String position;
        private String departments;
        private String phoneNumber;

        public String getPosition() {
            return position;
        }

        public String getDepartments() {
            return departments;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public String toString() {
            return "ExtraData{" +
                    "position='" + position + '\'' +
                    ", departments='" + departments + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    '}';
        }
    }
}
