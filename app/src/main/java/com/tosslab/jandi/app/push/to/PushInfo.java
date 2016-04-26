package com.tosslab.jandi.app.push.to;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PushInfo extends BasePushInfo {
    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("badge_count")
    private int badgeCount;
    @JsonProperty("team_id")
    private long teamId;
    @JsonProperty("team_name")
    private String teamName;
    @JsonProperty("room_id")
    private long roomId;
    @JsonProperty("room_name")
    private String roomName;
    @JsonProperty("room_type")
    private String roomType;
    @JsonProperty("message_id")
    private long messageId;
    @JsonProperty("message_type")
    private String messageType;
    @JsonProperty("message_content")
    private MessageContent messageContent;
    @JsonProperty("message_mentions")
    private List<Mention> mentions;
    @JsonProperty("message_feedback_id")
    private long messageFeedbackId;
    @JsonProperty("writer_id")
    private long writerId;
    @JsonProperty("writer_name")
    private String writerName;
    @JsonProperty("writer_type")
    private String writerType;
    @JsonProperty("writer_thumb")
    private String writerThumb;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageFeedbackId() {
        return messageFeedbackId;
    }

    public void setMessageFeedbackId(long messageFeedbackId) {
        this.messageFeedbackId = messageFeedbackId;
    }

    public void setWriterId(long writerId) {
        this.writerId = writerId;
    }

    public String getWriterType() {
        return writerType;
    }

    public void setWriterType(String writerType) {
        this.writerType = writerType;
    }

    public long getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public long getRoomId() {
        return roomId;
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

    public long getMessageId() {
        return messageId;
    }


    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(MessageContent messageContent) {
        this.messageContent = messageContent;
    }

    public List<Mention> getMentions() {
        return mentions;
    }

    public void setMentions(List<Mention> mentions) {
        this.mentions = mentions;
    }

    public long getWriterId() {
        return writerId;
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

    @Override
    public String toString() {
        return "PushInfo{" +
                "teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", roomType='" + roomType + '\'' +
                ", messageId=" + messageId +
                ", messageType='" + messageType + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", mentions=" + mentions +
                ", messageFeedbackId=" + messageFeedbackId +
                ", writerId=" + writerId +
                ", writerName='" + writerName + '\'' +
                ", writerType='" + writerType + '\'' +
                ", writerThumb='" + writerThumb + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Mention {
        private long id;
        private String type;
        private int offset;
        private int length;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MessageContent {
        private String body;
        private List<ResMessages.ConnectInfo> connectInfos;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public List<ResMessages.ConnectInfo> getConnectInfos() {
            return connectInfos;
        }

        public void setConnectInfos(List<ResMessages.ConnectInfo> connectInfos) {
            this.connectInfos = connectInfos;
        }
    }
}
