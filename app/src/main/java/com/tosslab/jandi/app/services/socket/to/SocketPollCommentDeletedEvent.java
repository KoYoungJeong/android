package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(1)
public class SocketPollCommentDeletedEvent implements EventHistoryInfo {
    private String event;
    private int version;
    private long ts;
    private long writer;
    private long teamId;
    private Data data;
    private String unique;

    public long getWriter() {
        return writer;
    }

    public void setWriter(long writer) {
        this.writer = writer;
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketPollCommentCreatedEvent{" +
                "event='" + event + '\'' +
                ", version=" + version +
                ", ts=" + ts +
                ", writer=" + writer +
                ", teamId=" + teamId +
                ", data=" + data +
                '}';
    }

    public static class Data {

        private Poll poll;
        private ResMessages.Link linkComment;

        public Poll getPoll() {
            return poll;
        }

        public void setPoll(Poll poll) {
            this.poll = poll;
        }

        public ResMessages.Link getLinkComment() {
            return linkComment;
        }

        public void setLinkComment(ResMessages.Link linkComment) {
            this.linkComment = linkComment;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "poll=" + poll +
                    ", linkComment=" + linkComment +
                    '}';
        }
    }
}
