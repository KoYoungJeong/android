package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(1)
public class SocketMessageDeletedEvent implements EventHistoryInfo {
    private int version;
    private String event;
    private long teamId;
    private long ts;
    private Data data;
    private String unique;

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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketMessageDeletedEvent that = (SocketMessageDeletedEvent) o;

        if (version != that.version) return false;
        if (teamId != that.teamId) return false;
        if (ts != that.ts) return false;
        if (event != null ? !event.equals(that.event) : that.event != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    public static class Data {
        private long linkId;
        private long messageId;
        private long roomId;

        public long getLinkId() {
            return linkId;
        }

        public void setLinkId(long linkId) {
            this.linkId = linkId;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            if (linkId != data.linkId) return false;
            if (messageId != data.messageId) return false;
            return roomId == data.roomId;

        }

        @Override
        public int hashCode() {
            int result = (int) (linkId ^ (linkId >>> 32));
            result = 31 * result + (int) (messageId ^ (messageId >>> 32));
            result = 31 * result + (int) (roomId ^ (roomId >>> 32));
            return result;
        }
    }
}
