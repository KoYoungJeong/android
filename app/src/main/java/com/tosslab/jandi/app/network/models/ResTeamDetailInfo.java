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

    private Member member;

    public InviteTeam getInviteTeam() {
        return inviteTeam;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "ResInvitationConfirm{" +
                "inviteTeam=" + inviteTeam +
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
    public static class Member {
        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

}
