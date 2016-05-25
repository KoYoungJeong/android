package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_bot")
public class Bot {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String botType;
    @DatabaseField
    private String name;
    @DatabaseField
    private String photoUrl;
    @DatabaseField
    private String status;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private InitializeInfo initialInfo;

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

    public String getBotType() {
        return botType;
    }

    public void setBotType(String botType) {
        this.botType = botType;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public InitializeInfo getInitialInfo() {
        return initialInfo;
    }

    public void setInitialInfo(InitializeInfo initialInfo) {
        this.initialInfo = initialInfo;
    }
}