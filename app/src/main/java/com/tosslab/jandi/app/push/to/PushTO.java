package com.tosslab.jandi.app.push.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 14. 12. 30..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PushTO {
    public enum RoomType {
        CHANNEL("channel"),
        PRIVATE_GROUP("privateGroup"),
        CHAT("chat");

        String name;

        RoomType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String action;
    private Alarm alarm;
    private PushInfo info;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public PushInfo getInfo() {
        return info;
    }

    public void setInfo(PushInfo info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "PushTO{" +
                "action='" + action + '\'' +
                ", alarm=" + alarm +
                ", info=" + info +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class PushInfo {
        private int badge;
        private int teamId;
        private String teamName;
        private int roomId;
        private String roomName;
        private String roomType;
        private int messageId;
        private String messageType;
        private String messageContent;

        // TODO MENTION !
        private Mention mention;

        private int writerId;
        private String writerName;
        private String writerThumb;
        private String createdAt;

        public int getBadge() {
            return badge;
        }

        public void setBadge(int badge) {
            this.badge = badge;
        }

        public int getTeamId() {
            return teamId;
        }

        public void setTeamId(int teamId) {
            this.teamId = teamId;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public int getRoomId() {
            return roomId;
        }

        public void setRoomId(int roomId) {
            this.roomId = roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public String getRoomType() {
            return roomType;
        }

        public void setRoomType(String roomType) {
            this.roomType = roomType;
        }

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }

        public Mention getMention() {
            return mention;
        }

        public void setMention(Mention mention) {
            this.mention = mention;
        }

        public int getWriterId() {
            return writerId;
        }

        public void setWriterId(int writerId) {
            this.writerId = writerId;
        }

        public String getWriterName() {
            return writerName;
        }

        public void setWriterName(String writerName) {
            this.writerName = writerName;
        }

        public String getWriterThumb() {
            return writerThumb;
        }

        public void setWriterThumb(String writerThumb) {
            this.writerThumb = writerThumb;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "PushInfo{" +
                    "badge=" + badge +
                    ", teamId=" + teamId +
                    ", teamName='" + teamName + '\'' +
                    ", roomId=" + roomId +
                    ", roomName='" + roomName + '\'' +
                    ", roomType=" + roomType +
                    ", messageId=" + messageId +
                    ", messageType=" + messageType +
                    ", messageContent='" + messageContent + '\'' +
                    ", mention=" + mention +
                    ", writerId=" + writerId +
                    ", writerName='" + writerName + '\'' +
                    ", writerThumb='" + writerThumb + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Alarm {
        private boolean topicSubscription = true;
        private boolean platformActive = false;

        public boolean isTopicSubscription() {
            return topicSubscription;
        }

        public void setTopicSubscription(boolean topicSubscription) {
            this.topicSubscription = topicSubscription;
        }

        public boolean isPlatformActive() {
            return platformActive;
        }

        public void setPlatformActive(boolean platformActive) {
            this.platformActive = platformActive;
        }

        @Override
        public String toString() {
            return "Alarm{" +
                    "topicSubscription=" + topicSubscription +
                    ", platformActive=" + platformActive +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Mention {
        private int id;
        private String type;
        private int offset;
        private int length;

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

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        @Override
        public String toString() {
            return "Mention{" +
                    "id=" + id +
                    ", type='" + type + '\'' +
                    ", offset=" + offset +
                    ", length=" + length +
                    '}';
        }
    }

}
