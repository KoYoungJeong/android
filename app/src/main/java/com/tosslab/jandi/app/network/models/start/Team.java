package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_team")
public class Team {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String status;
    @DatabaseField
    private String domain;
    @DatabaseField
    private String emailDomain;
    @DatabaseField
    private String invitationStatus;
    @DatabaseField
    private String invitationCode;
    @DatabaseField
    private String invitationUrl;
    @DatabaseField
    private String connectAuth;

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
