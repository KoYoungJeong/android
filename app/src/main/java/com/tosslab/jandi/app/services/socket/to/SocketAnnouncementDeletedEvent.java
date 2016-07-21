package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(1)
public class SocketAnnouncementDeletedEvent implements EventHistoryInfo {
    private String event;
    private int version;
    private long teamId;
    private long ts;
    private Data data;

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketAnnouncementDeletedEvent{" +
                "event='" + event + '\'' +
                ", version=" + version +
                ", data=" + data +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private long topicId;
        private long teamId;

        public long getTopicId() {
            return topicId;
        }

        public void setTopicId(long topicId) {
            this.topicId = topicId;
        }

        public long getTeamId() {
            return teamId;
        }

        public void setTeamId(long teamId) {
            this.teamId = teamId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            if (topicId != data.topicId) return false;
            return teamId == data.teamId;

        }

        @Override
        public int hashCode() {
            int result = (int) (topicId ^ (topicId >>> 32));
            result = 31 * result + (int) (teamId ^ (teamId >>> 32));
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketAnnouncementDeletedEvent that = (SocketAnnouncementDeletedEvent) o;

        if (version != that.version) return false;
        if (teamId != that.teamId) return false;
        if (ts != that.ts) return false;
        if (event != null ? !event.equals(that.event) : that.event != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
