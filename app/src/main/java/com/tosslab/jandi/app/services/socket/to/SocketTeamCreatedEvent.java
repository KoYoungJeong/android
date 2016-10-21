package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by tee on 2016. 10. 21..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(2)
public class SocketTeamCreatedEvent implements EventHistoryInfo {

    private String event;
    private int version;
    private long ts;
    private long teamId;
    private Data data;
    private String unique;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @Override
    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private Team team;
        private long memberId;

        public Team getTeam() {
            return team;
        }

        public void setTeam(Team team) {
            this.team = team;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Team {
        private long teamId;
        private long id;
        private String invitationCode;
        private String t_emailDomain;
        private String t_domain;
        private String anme;
        private String connectAuth;
        private String invitationStatus;
        private long t_defaultChannelId;
        private long[] t_members;
        private String status;
        private String type;
        private String invitationUrl;

        public long getTeamId() {
            return teamId;
        }

        public void setTeamId(long teamId) {
            this.teamId = teamId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getInvitationCode() {
            return invitationCode;
        }

        public void setInvitationCode(String invitationCode) {
            this.invitationCode = invitationCode;
        }

        public String getT_emailDomain() {
            return t_emailDomain;
        }

        public void setT_emailDomain(String t_emailDomain) {
            this.t_emailDomain = t_emailDomain;
        }

        public String getT_domain() {
            return t_domain;
        }

        public void setT_domain(String t_domain) {
            this.t_domain = t_domain;
        }

        public String getAnme() {
            return anme;
        }

        public void setAnme(String anme) {
            this.anme = anme;
        }

        public String getConnectAuth() {
            return connectAuth;
        }

        public void setConnectAuth(String connectAuth) {
            this.connectAuth = connectAuth;
        }

        public String getInvitationStatus() {
            return invitationStatus;
        }

        public void setInvitationStatus(String invitationStatus) {
            this.invitationStatus = invitationStatus;
        }

        public long getT_defaultChannelId() {
            return t_defaultChannelId;
        }

        public void setT_defaultChannelId(long t_defaultChannelId) {
            this.t_defaultChannelId = t_defaultChannelId;
        }

        public long[] getT_members() {
            return t_members;
        }

        public void setT_members(long[] t_members) {
            this.t_members = t_members;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getInvitationUrl() {
            return invitationUrl;
        }

        public void setInvitationUrl(String invitationUrl) {
            this.invitationUrl = invitationUrl;
        }
    }

}
