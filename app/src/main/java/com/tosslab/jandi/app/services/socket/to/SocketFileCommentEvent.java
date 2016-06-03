package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketFileCommentEvent extends SocketFileEvent {
    private long writer;
    private EventCommentInfo comment;

    public EventCommentInfo getComment() {
        return comment;
    }

    public void setComment(EventCommentInfo comment) {
        this.comment = comment;
    }

    public long getWriter() {
        return writer;
    }

    public void setWriter(long writer) {
        this.writer = writer;
    }

    @Override
    public String toString() {
        return "SocketFileCommentEvent{" +
                "writer=" + writer +
                ", comment=" + comment +
                super.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class EventCommentInfo {
        private long id;
        private long linkId;
        private List<Long> shareEntities;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getLinkId() {
            return linkId;
        }

        public void setLinkId(long linkId) {
            this.linkId = linkId;
        }

        public List<Long> getShareEntities() {
            return shareEntities;
        }

        public void setShareEntities(List<Long> shareEntities) {
            this.shareEntities = shareEntities;
        }

        @Override
        public String toString() {
            return "EventCommentInfo{" +
                    "id=" + id +
                    ", linkId=" + linkId +
                    ", shareEntities=" + shareEntities +
                    '}';
        }
    }
}
