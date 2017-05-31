package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vimeo.stag.GsonAdapterKey;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Human {
    @GsonAdapterKey
    long id;
    @GsonAdapterKey
    long teamId;
    @GsonAdapterKey
    String type;
    @GsonAdapterKey
    String name;
    @GsonAdapterKey
    String photoUrl;
    @GsonAdapterKey
    String accountId;
    @GsonAdapterKey
    Profile profile;
    @GsonAdapterKey
    String status;
    @GsonAdapterKey
    List<Long> joinTopics;
    @GsonAdapterKey
    long rankId;
    @GsonAdapterKey
    boolean isStarred;
    @GsonAdapterKey
    Absence absence;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public void setIsStarred(boolean starred) {
        isStarred = starred;
    }

    public long getRankId() {
        return rankId;
    }

    public Human setRankId(long rankId) {
        this.rankId = rankId;
        return this;
    }

    public Absence getAbsence() {
        return absence;
    }

    public void setAbsence(Absence absence) {
        this.absence = absence;
    }

    public List<Long> getJoinTopics() {
        return joinTopics;
    }

    public void setJoinTopics(List<Long> joinTopics) {
        this.joinTopics = joinTopics;
    }
}
