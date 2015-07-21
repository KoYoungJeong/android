package com.tosslab.jandi.app.push.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 14. 12. 30..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PushTO {

    private String action;
    private String type;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type",
            defaultImpl = PushInfo.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = PushTO.MessagePush.class, name = "push"),
            @JsonSubTypes.Type(value = PushTO.SubscribePush.class, name = "subscribe"),
            @JsonSubTypes.Type(value = PushTO.UnSubscribePush.class, name = "unsubscribe")})
    private PushInfo info;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PushInfo getInfo() {
        return info;
    }

    public void setInfo(PushInfo info) {
        this.info = info;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PushInfo {

        private String alert;

        public String getAlert() {
            return alert;
        }

        public void setAlert(String alert) {
            this.alert = alert;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MessagePush extends PushInfo {
        private int chatId;
        private String contentType;
        private String chatName;
        private String chatType;
        private int writerId;
        private String writerName;
        private String writerThumb;
        private int teamId;
        private String teamName;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getChatName() {
            return chatName;
        }

        public void setChatName(String chatName) {
            this.chatName = chatName;
        }

        public String getChatType() {
            return chatType;
        }

        public void setChatType(String chatType) {
            this.chatType = chatType;
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

        public int getChatId() {
            return chatId;
        }

        public void setChatId(int chatId) {
            this.chatId = chatId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class SubscribePush extends PushInfo {
        private String chatId;

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class UnSubscribePush extends PushInfo {
        private String chatId;

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }
}
