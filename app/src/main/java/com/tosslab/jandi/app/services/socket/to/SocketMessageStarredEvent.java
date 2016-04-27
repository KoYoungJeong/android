package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by Steve SeongUg Jung on 15. 8. 5..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketMessageStarredEvent {

    private String event;
    private int version;
    @JsonProperty("data")
    private StarredInfo starredInfo;

    private long ts;

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }


    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public StarredInfo getStarredInfo() {
        return starredInfo;
    }

    public void setStarredInfo(StarredInfo starredInfo) {
        this.starredInfo = starredInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class StarredInfo {
        private int memberId;
        private int messageId;
        private int teamId;

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        public int getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }
    }
}
