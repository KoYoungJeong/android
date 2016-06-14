package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@Version(1)
public class SocketTopicKickedoutEvent implements EventHistoryInfo {
    private String event;
    private int version;
    private Data data;

    private long ts;
    private long teamId;

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
        return "SocketTopicKickedoutEvent{" +
                "event='" + event + '\'' +
                ", version='" + version + '\'' +
                ", data=" + data +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private int roomId;
        private int teamId;

        public int getRoomId() {
            return roomId;
        }

        public void setRoomId(int roomId) {
            this.roomId = roomId;
        }

        public int getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "roomId=" + roomId +
                    ", teamId=" + teamId +
                    '}';
        }
    }
}
