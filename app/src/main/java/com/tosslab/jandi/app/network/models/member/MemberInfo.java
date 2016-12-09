package com.tosslab.jandi.app.network.models.member;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MemberInfo {
    private long id;
    private long teamId;
    private String name;
    private String type;
    private String photoUrl;
    private String role;
    private String accountId;
    private String status;
    private long rankId;
    private boolean isStarred;
    private List<Long> joinTopics;

    public long getId() {
        return id;
    }

    public MemberInfo setId(long id) {
        this.id = id;
        return this;
    }

    public long getTeamId() {
        return teamId;
    }

    public MemberInfo setTeamId(long teamId) {
        this.teamId = teamId;
        return this;
    }

    public String getName() {
        return name;
    }

    public MemberInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public MemberInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public MemberInfo setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public String getRole() {
        return role;
    }

    public MemberInfo setRole(String role) {
        this.role = role;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public MemberInfo setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public MemberInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    public long getRankId() {
        return rankId;
    }

    public MemberInfo setRankId(long rankId) {
        this.rankId = rankId;
        return this;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public MemberInfo setStarred(boolean starred) {
        isStarred = starred;
        return this;
    }

    public List<Long> getJoinTopics() {
        return joinTopics;
    }

    public MemberInfo setJoinTopics(List<Long> joinTopics) {
        this.joinTopics = joinTopics;
        return this;
    }
}
