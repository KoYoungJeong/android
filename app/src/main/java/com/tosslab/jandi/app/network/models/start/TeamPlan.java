package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vimeo.stag.GsonAdapterKey;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TeamPlan {
    @GsonAdapterKey
    long teamId;
    @GsonAdapterKey
    String pricing;
    @GsonAdapterKey
    long fileSize;
    @GsonAdapterKey
    long messageCount;
    @GsonAdapterKey
    boolean isExceedFile;
    @GsonAdapterKey
    boolean isExceedMessage;
    @GsonAdapterKey
    Date updatedAt;

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

    public void setIsExceedFile(boolean exceedFile) {
        isExceedFile = exceedFile;
    }

    public boolean isExceedMessage() {
        return isExceedMessage;
    }

    public void setIsExceedMessage(boolean exceedMessage) {
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

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;

    }
}
