package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.util.Date;

/**
 * Created by tee on 2017. 5. 29..
 */

public class ReqAbsenceInfo {

    private String status;
    private boolean disablePush;
    private String message;
    private String startAt;
    private String endAt;

    public ReqAbsenceInfo(String status, boolean disablePush, String message, Date startAt, Date endAt) {
        this.status = status;
        this.disablePush = disablePush;
        this.message = message;
        this.startAt = ISO8601Utils.format(startAt);
        this.endAt = ISO8601Utils.format(endAt);
    }

    public ReqAbsenceInfo(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDisablePush() {
        return disablePush;
    }

    public void setDisablePush(boolean disablePush) {
        this.disablePush = disablePush;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(Date startAt) {
        this.startAt = ISO8601Utils.format(startAt);
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(Date endAt) {
        this.endAt = ISO8601Utils.format(endAt);
    }

    @Override
    public String toString() {
        return "ReqAbsenceInfo{" +
                "status='" + status + '\'' +
                ", disablePush=" + disablePush +
                ", message='" + message + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }
}
