package com.tosslab.jandi.app.services.socket.to;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketMemberRankUpdatedEvent implements EventHistoryInfo {

    private String event;
    private int version;
    private long teamId;
    private long ts;
    private Data data;
    private String unique;

    @Override
    public long getTs() {
        return ts;
    }

    public SocketMemberRankUpdatedEvent setTs(long ts) {
        this.ts = ts;
        return this;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public SocketMemberRankUpdatedEvent setEvent(String event) {
        this.event = event;
        return this;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public SocketMemberRankUpdatedEvent setVersion(int version) {
        this.version = version;
        return this;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    public SocketMemberRankUpdatedEvent setTeamId(long teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public String getUnique() {
        return unique;
    }

    public SocketMemberRankUpdatedEvent setUnique(String unique) {
        this.unique = unique;
        return this;
    }

    public Data getData() {
        return data;
    }

    public SocketMemberRankUpdatedEvent setData(Data data) {
        this.data = data;
        return this;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private long rankId;
        private List<Long> memberIds;

        public long getRankId() {
            return rankId;
        }

        public Data setRankId(long rankId) {
            this.rankId = rankId;
            return this;
        }

        public List<Long> getMemberIds() {
            return memberIds;
        }

        public Data setMemberIds(List<Long> memberIds) {
            this.memberIds = memberIds;
            return this;
        }
    }
}
