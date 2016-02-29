package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.services.socket.annotations.Version;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketMessageEvent {
    private int version;
    private String event;
    private String messageType;
    private int messageId;
    private int teamId;
    private MessageRoom room;
    private int writer;
    private List<MessageRoom> rooms;
    private CommentInfo comment;
    private long linkId;
    private long ts;

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }


    @Override
    public String toString() {
        return "SocketMessageEvent{" +
                "version=" + version +
                ", event='" + event + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messageId=" + messageId +
                ", teamId=" + teamId +
                ", room=" + room +
                ", writer=" + writer +
                ", rooms=" + rooms +
                ", comment=" + comment +
                '}';
    }

    public MessageRoom getRoom() {
        return room;
    }

    public void setRoom(MessageRoom room) {
        this.room = room;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<MessageRoom> getRooms() {
        return rooms;
    }

    public void setRooms(List<MessageRoom> rooms) {
        this.rooms = rooms;
    }

    public CommentInfo getComment() {
        return comment;
    }

    public void setComment(CommentInfo comment) {
        this.comment = comment;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getWriter() {
        return writer;
    }

    public void setWriter(int writer) {
        this.writer = writer;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MessageRoom {
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
    public static class CommentInfo {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

}
