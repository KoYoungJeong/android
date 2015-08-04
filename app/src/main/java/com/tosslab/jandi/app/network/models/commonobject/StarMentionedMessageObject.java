package com.tosslab.jandi.app.network.models.commonobject;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;

/**
 * Created by tee on 15. 7. 29..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class StarMentionedMessageObject {
    private int teamId;
    private int linkId;
    private Date createdAt;
    private Room room;
    private Message message;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "StarMentionedMessageObject{" +
                "teamId=" + teamId +
                ", linkId=" + linkId +
                ", createdAt=" + createdAt +
                ", room=" + room.toString() +
                ", message=" + message.toString() +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class Room {
        public int id;
        public String type;
        public String name;

        public Room() {
        }

        @Override
        public String toString() {
            return "Room{" +
                    "id=" + id +
                    ", type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class Message {
        public int id;
        public int writerId;
        public String contentType;
        public String contentTitle;
        public String contentBody;
        public String contentExtension;
        public List<MentionObject> mentions;
        public int feedbackId;
        public String feedbackTitle;
        public int commentCount;
        public Date createAt;

        public Message() {
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", writerId=" + writerId +
                    ", contentType='" + contentType + '\'' +
                    ", contentTitle='" + contentTitle + '\'' +
                    ", contentBody='" + contentBody + '\'' +
                    ", contentExtension='" + contentExtension + '\'' +
                    ", mentions=" + mentions +
                    ", feedbackId=" + feedbackId +
                    ", feedbackTitle='" + feedbackTitle + '\'' +
                    ", commentCount=" + commentCount +
                    ", createAt=" + createAt +
                    '}';
        }
    }
}