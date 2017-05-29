package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(2)
public class SocketMemberUpdatedEvent implements EventHistoryInfo {
    private int version;
    private String event;
    private long teamId;
    private Data data;
    private long ts;
    private String unique;

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
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

    @Override
    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private Human member;
        private String status;
        private Absence absence;

        public Human getMember() {
            return member;
        }

        public void setMember(Human member) {
            this.member = member;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public static class Absence {
            private String message;
            private Date StartAt;
            private Date endAt;

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }

            public Date getStartAt() {
                return StartAt;
            }

            public void setStartAt(Date startAt) {
                StartAt = startAt;
            }

            public Date getEndAt() {
                return endAt;
            }

            public void setEndAt(Date endAt) {
                this.endAt = endAt;
            }
        }
    }

}
