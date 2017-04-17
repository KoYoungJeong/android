package com.tosslab.jandi.app.network.models.invite;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vimeo.stag.GsonAdapterKey;

import java.util.Date;

/**
 * Created by tee on 2017. 4. 6..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Invitation {
    @GsonAdapterKey
    long inviteMemberId;
    @GsonAdapterKey
    String inviteeEmail;
    @GsonAdapterKey
    String inviteeAccountId;
    @GsonAdapterKey
    long inviterMemberId;
    @GsonAdapterKey
    String teamDomain;
    @GsonAdapterKey
    String teamName;
    @GsonAdapterKey
    long teamId;
    @GsonAdapterKey
    long v;
    @GsonAdapterKey
    Date updatedAt;
    @GsonAdapterKey
    Date createdAt;
    @GsonAdapterKey
    Date tokenGeneratedAt;
    @GsonAdapterKey
    String status;
    @GsonAdapterKey
    String role;
    @GsonAdapterKey
    String inviteeAccountDetailUuid;
    @GsonAdapterKey
    String id;

    public long getInviteMemberId() {
        return inviteMemberId;
    }

    public void setInviteMemberId(long inviteMemberId) {
        this.inviteMemberId = inviteMemberId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public String getInviteeAccountId() {
        return inviteeAccountId;
    }

    public void setInviteeAccountId(String inviteeAccountId) {
        this.inviteeAccountId = inviteeAccountId;
    }

    public long getInviterMemberId() {
        return inviterMemberId;
    }

    public void setInviterMemberId(long inviterMemberId) {
        this.inviterMemberId = inviterMemberId;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public void setTeamDomain(String teamDomain) {
        this.teamDomain = teamDomain;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getV() {
        return v;
    }

    public void setV(long v) {
        this.v = v;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getTokenGeneratedAt() {
        return tokenGeneratedAt;
    }

    public void setTokenGeneratedAt(Date tokenGeneratedAt) {
        this.tokenGeneratedAt = tokenGeneratedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getInviteeAccountDetailUuid() {
        return inviteeAccountDetailUuid;
    }

    public void setInviteeAccountDetailUuid(String inviteeAccountDetailUuid) {
        this.inviteeAccountDetailUuid = inviteeAccountDetailUuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
