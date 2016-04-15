package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResPendingTeamInfo {

    private String toEmail;
    private long memberId;
    private long teamId;
    private String teamName;
    private String teamDomain;
    private String token;
    private String updatedAt;
    private String createdAt;
    private String status;
    private String role;
    private String toAccountId;
    private String id;

    public String getToEmail() {
        return toEmail;
    }

    public long getMemberId() {
        return memberId;
    }

    public long getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public String getToken() {
        return token;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ResPendingInvitation{" +
                "toEmail='" + toEmail + '\'' +
                ", memberId=" + memberId +
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", teamDomain='" + teamDomain + '\'' +
                ", token='" + token + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                ", toAccountId='" + toAccountId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
