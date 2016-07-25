package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 14..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(1)
public class SocketRoomMarkerEvent implements EventHistoryInfo {
    private int version;
    private String event;
    private long teamId;
    private MarkerRoom room;
    private Marker marker;
    private long ts;

    @Override
    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

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

    public MarkerRoom getRoom() {
        return room;
    }

    public void setRoom(MarkerRoom room) {
        this.room = room;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MarkerRoom {
        private long id;
        private String type;
        private List<Long> members;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Long> getMembers() {
            return members;
        }

        public void setMembers(List<Long> members) {
            this.members = members;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MarkerRoom that = (MarkerRoom) o;

            if (id != that.id) return false;
            if (type != null ? !type.equals(that.type) : that.type != null) return false;
            return members != null ? members.equals(that.members) : that.members == null;

        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (members != null ? members.hashCode() : 0);
            return result;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Marker {

        private long memberId;
        private long lastLinkId;

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public long getLastLinkId() {
            return lastLinkId;
        }

        public void setLastLinkId(long lastLinkId) {
            this.lastLinkId = lastLinkId;
        }

    }
}

