package com.tosslab.jandi.app.network.models.team.rank;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Rank extends RealmObject {
    @PrimaryKey
    private long id;
    private String name;
    private int level;
    private long teamId;
    private Date updatedAt;
    private Date createdAt;
    private String status;

    public long getId() {
        return id;
    }

    public Rank setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Rank setName(String name) {
        this.name = name;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public Rank setLevel(int level) {
        this.level = level;
        return this;
    }

    public long getTeamId() {
        return teamId;
    }

    public Rank setTeamId(long teamId) {
        this.teamId = teamId;
        return this;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Rank setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Rank setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Rank setStatus(String status) {
        this.status = status;
        return this;
    }
}
