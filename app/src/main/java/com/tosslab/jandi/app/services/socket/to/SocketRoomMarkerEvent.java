package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 14..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketRoomMarkerEvent {
    private String event;
    private MarkerRoom room;
    private Marker marker;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MarkerRoom {
        private int id;
        private String type;
        private List<Integer> members;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Integer> getMembers() {
            return members;
        }

        public void setMembers(List<Integer> members) {
            this.members = members;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Marker {

        @JsonProperty("member")
        private int memberId;
        private int fromLinkId;
        private int toLinkId;

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public int getFromLinkId() {
            return fromLinkId;
        }

        public void setFromLinkId(int fromLinkId) {
            this.fromLinkId = fromLinkId;
        }

        public int getToLinkId() {
            return toLinkId;
        }

        public void setToLinkId(int toLinkId) {
            this.toLinkId = toLinkId;
        }
    }
}
