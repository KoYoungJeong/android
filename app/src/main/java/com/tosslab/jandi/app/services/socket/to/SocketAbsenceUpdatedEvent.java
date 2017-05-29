package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.Date;

/**
 * Created by tee on 2017. 5. 29..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketAbsenceUpdatedEvent implements EventHistoryInfo {

    private String event;
    private int version;
    private long ts;
    private long teamId;
    private SocketAbsenceUpdatedEvent.Data data;
    private String unique;

    @Override
    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @Override
    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Absence {
        private String status;
        private String applyStatus;
        private String message;
        private Date startAt;
        private Date endAt;
        private boolean disablePush;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getApplyStatus() {
            return applyStatus;
        }

        public void setApplyStatus(String applyStatus) {
            this.applyStatus = applyStatus;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Date getStartAt() {
            return startAt;
        }

        public void setStartAt(Date startAt) {
            this.startAt = startAt;
        }

        public Date getEndAt() {
            return endAt;
        }

        public void setEndAt(Date endAt) {
            this.endAt = endAt;
        }

        public boolean isDisablePush() {
            return disablePush;
        }

        public void setDisablePush(boolean disablePush) {
            this.disablePush = disablePush;
        }
    }

    public class Data {

        private Absence absence;

        public Absence getAbsence() {
            return absence;
        }

        public void setAbsence(Absence absence) {
            this.absence = absence;
        }
    }
}
