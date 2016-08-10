package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by tonyjs on 15. 7. 30..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(1)
public class SocketTopicPushEvent implements EventHistoryInfo {

    private String event;
    private int version;
    private Data data;

    private long ts;
    private long teamId;
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

    @Override
    public String getUnique() {
        return unique;
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
        return "SocketTopicPushEvent{" +
                "data=" + data +
                ", version=" + version +
                ", event='" + event + '\'' +
                '}';
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private long roomId;
        private boolean subscribe = false;

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public boolean isSubscribe() {
            return subscribe;
        }

        public void setSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "roomId=" + roomId +
                    ", subscribe=" + subscribe +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            if (roomId != data.roomId) return false;
            return subscribe == data.subscribe;

        }

        @Override
        public int hashCode() {
            int result = (int) (roomId ^ (roomId >>> 32));
            result = 31 * result + (subscribe ? 1 : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketTopicPushEvent that = (SocketTopicPushEvent) o;

        if (version != that.version) return false;
        if (ts != that.ts) return false;
        if (teamId != that.teamId) return false;
        if (event != null ? !event.equals(that.event) : that.event != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        return result;
    }
}
