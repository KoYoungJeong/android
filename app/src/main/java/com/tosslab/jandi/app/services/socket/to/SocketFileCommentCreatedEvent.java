package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(1)
public class SocketFileCommentCreatedEvent implements EventHistoryInfo {
    private long writer;
    private EventCommentInfo comment;
    private long teamId;
    private long ts;
    private int version;
    private EventFileInfo file;
    private String event;
    private String unique;

    public SocketFileCommentCreatedEvent() {
        event = "file_comment_created";
    }

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
        return "SocketFileCommentCreatedEvent{" +
                "writer=" + writer +
                ", comment=" + comment +
                super.toString();
    }

    public EventFileInfo getFile() {
        return file;
    }

    public void setFile(EventFileInfo file) {
        this.file = file;
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

    @Override
    public String getUnique() {
        return unique;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
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

    public void setUnique(String unique) {
        this.unique = unique;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class EventFileInfo {
        private int commentCount;
        private long id;

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "FileInfo{" +
                    "commentCount=" + commentCount +
                    ", id=" + id +
                    '}';
        }
    }
}
