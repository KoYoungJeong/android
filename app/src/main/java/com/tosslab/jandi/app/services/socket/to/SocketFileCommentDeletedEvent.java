package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@Version(1)
public class SocketFileCommentDeletedEvent implements EventHistoryInfo {
    private EventCommentInfo comment;
    private List<Room> rooms;
    private long teamId;
    private long ts;
    private int version;
    private EventFileInfo file;
    private String event;

    public EventCommentInfo getComment() {
        return comment;
    }

    public void setComment(EventCommentInfo comment) {
        this.comment = comment;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
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
    public static class Room {
        private long id;
        private String type;
        private long[] members;

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

        public long[] getMembers() {
            return members;
        }

        public void setMembers(long[] members) {
            this.members = members;
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
