package com.tosslab.jandi.app.network.models.commonobject;

import com.tosslab.jandi.app.network.models.ResMessages;

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
    private long teamId;
    private long linkId;
    private Date createdAt;
    private long starredId;
    private Room room;
    private Message message;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
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

    public long getStarredId() {
        return starredId;
    }

    public void setStarredId(long starredId) {
        this.starredId = starredId;
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
    public static class Room {
        public long id;
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
    public static class Message {
        public long id;
        public long writerId;
        public String contentType;

        public List<MentionObject> mentions;
        public long feedbackId;
        public String feedbackTitle;
        public int commentCount;
        public Date createdAt;
        public Content content;

        public Message() {
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", writerId=" + writerId +
                    ", mentions=" + mentions +
                    ", feedbackId=" + feedbackId +
                    ", feedbackTitle='" + feedbackTitle + '\'' +
                    ", commentCount=" + commentCount +
                    ", createdAt=" + createdAt +
                    ", content=" + content.toString() +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public static class Content {

            public String body;
            public String title;
            public String name;
            public String filename;
            public String filterType;
            public String type;
            public String icon;
            public String size;
            public String ext;
            public String serverUrl;
            public String fileUrl;
            public ResMessages.ThumbnailUrls extraInfo;

            public Content() {

            }

            @Override
            public String toString() {
                return "Content{" +
                        "body='" + body + '\'' +
                        ", title='" + title + '\'' +
                        ", name='" + name + '\'' +
                        ", filename='" + filename + '\'' +
                        ", filterType='" + filterType + '\'' +
                        ", type='" + type + '\'' +
                        ", icon='" + icon + '\'' +
                        ", size='" + size + '\'' +
                        ", ext='" + ext + '\'' +
                        ", serverUrl='" + serverUrl + '\'' +
                        ", fileUrl='" + fileUrl + '\'' +
                        ", extraInfo=" + extraInfo +
                        '}';
            }
        }
    }
}