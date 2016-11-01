package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TeamPlan extends RealmObject {
    @PrimaryKey
    private long teamId;
    private String pricing;
    private long fileSize;
    private boolean isExceedFile;
    private boolean isExceedMessage;
    private Date updatedAt;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getPricing() {
        return pricing;
    }

    public void setPricing(String pricing) {
        this.pricing = pricing;
    }

    public boolean isExceedFile() {
        return isExceedFile;
    }

    public void setExceedFile(boolean exceedFile) {
        isExceedFile = exceedFile;
    }

    public boolean isExceedMessage() {
        return isExceedMessage;
    }

    public void setExceedMessage(boolean exceedMessage) {
        isExceedMessage = exceedMessage;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
