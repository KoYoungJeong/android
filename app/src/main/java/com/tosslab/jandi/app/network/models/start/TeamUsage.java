package com.tosslab.jandi.app.network.models.start;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.vimeo.stag.GsonAdapterKey;

import java.util.Date;
import java.util.List;

/**
 * Created by tee on 2017. 3. 2..
 */

public class TeamUsage {

    @GsonAdapterKey
    long teamId;
    @GsonAdapterKey
    long limitedLinkId;
    @GsonAdapterKey
    long connectCount;
    @GsonAdapterKey
    long connectTypeCount;
    @GsonAdapterKey
    List<ConnectDetail> connectDetail;
    @GsonAdapterKey
    long fileCount;
    @GsonAdapterKey
    long fileSize;
    @GsonAdapterKey
    long messageCount;
    @GsonAdapterKey
    Date fileUploadAt;
    @GsonAdapterKey
    Date messageUpdateAt;
    @GsonAdapterKey
    Date updateAt;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getLimitedLinkId() {
        if (!TeamInfoLoader.getInstance().getTeamPlan().getPricing().equals("free")) {
            return -1;
        }
        return limitedLinkId;
    }

    public void setLimitedLinkId(long limitedLinkId) {
        this.limitedLinkId = limitedLinkId;
    }

    public long getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(long connectCount) {
        this.connectCount = connectCount;
    }

    public long getConnectTypeCount() {
        return connectTypeCount;
    }

    public void setConnectTypeCount(long connectTypeCount) {
        this.connectTypeCount = connectTypeCount;
    }

    public List<ConnectDetail> getConnectDetail() {
        return connectDetail;
    }

    public void setConnectDetail(List<ConnectDetail> connectDetail) {
        this.connectDetail = connectDetail;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
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

    public Date getFileUploadAt() {
        return fileUploadAt;
    }

    public void setFileUploadAt(Date fileUploadAt) {
        this.fileUploadAt = fileUploadAt;
    }

    public Date getMessageUpdateAt() {
        return messageUpdateAt;
    }

    public void setMessageUpdateAt(Date messageUpdateAt) {
        this.messageUpdateAt = messageUpdateAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public static class ConnectDetail {
        @GsonAdapterKey
        String type;
        @GsonAdapterKey
        long count;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
