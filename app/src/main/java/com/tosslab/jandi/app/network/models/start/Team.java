package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vimeo.stag.GsonAdapterKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Team {
    @GsonAdapterKey
    long id;
    @GsonAdapterKey
    String name;
    @GsonAdapterKey
    String status;
    @GsonAdapterKey
    String domain;
    @GsonAdapterKey
    String emailDomain;
    @GsonAdapterKey
    String invitationStatus;
    @GsonAdapterKey
    String invitationCode;
    @GsonAdapterKey
    String invitationUrl;
    @GsonAdapterKey
    String connectAuth;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
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

    public String getConnectAuth() {
        return connectAuth;
    }

    public void setConnectAuth(String connectAuth) {
        this.connectAuth = connectAuth;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", domain='" + domain + '\'' +
                ", emailDomain='" + emailDomain + '\'' +
                ", invitationStatus='" + invitationStatus + '\'' +
                ", invitationCode='" + invitationCode + '\'' +
                ", invitationUrl='" + invitationUrl + '\'' +
                ", connectAuth='" + connectAuth + '\'' +
                '}';
    }
}
