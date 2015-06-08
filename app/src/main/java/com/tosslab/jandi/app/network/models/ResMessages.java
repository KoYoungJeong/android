package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justinygchoi on 2014. 6. 18..
 * CDP 메시지 리스트 획득의 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMessages {
    public int entityId;
    public int lastLinkId;
    public int firstLinkId;
    public List<Link> records;

    @Override
    public String toString() {
        return "ResMessages{" +
                "lastLinkId=" + lastLinkId +
                ", firstLinkId=" + firstLinkId +
                ", records=" + records +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Link {
        public int id;
        public int teamId;
        public int fromEntity;
        public Date time;
        public int messageId;
        public String status;
        public int feedbackId;

        public Map<String, Object> info; // How to convert other type
        public OriginalMessage feedback;
        public OriginalMessage message;

        @Override
        public String toString() {
            return "Link{" +
                    "id=" + id +
                    ", teamId=" + teamId +
                    ", fromEntity=" + fromEntity +
                    ", time=" + time +
                    ", messageId=" + messageId +
                    ", status='" + status + '\'' +
                    ", feedbackId=" + feedbackId +
                    ", info=" + info +
                    ", feedback=" + feedback +
                    ", message=" + message +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Info {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "eventType",
            defaultImpl = EventInfo.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = CreateEvent.class, name = "create"),
            @JsonSubTypes.Type(value = InviteEvent.class, name = "invite"),
            @JsonSubTypes.Type(value = LeaveEvent.class, name = "leave"),
            @JsonSubTypes.Type(value = JoinEvent.class, name = "join")})
    public static class EventInfo {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "contentType")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TextMessage.class, name = "text"),
            @JsonSubTypes.Type(value = FileMessage.class, name = "file"),
            @JsonSubTypes.Type(value = StickerMessage.class, name = "sticker"),
            @JsonSubTypes.Type(value = CommentMessage.class, name = "comment")})
    public static class OriginalMessage {
        public int id;
        public int teamId;
        public int writerId;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public String status;
        public List<Integer> shareEntities;
        public int permission;
        public int feedbackId;
        public FileMessage feedback;

        @Override
        public String toString() {
            return "OriginalMessage{" +
                    "id=" + id +
                    ", teamId=" + teamId +
                    ", writerId=" + writerId +
                    ", createTime=" + createTime +
                    ", updateTime=" + updateTime +
                    ", contentType='" + contentType + '\'' +
                    ", status='" + status + '\'' +
                    ", shareEntities=" + shareEntities +
                    ", permission=" + permission +
                    ", feedbackId=" + feedbackId +
                    ", feedback=" + feedback +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextMessage extends OriginalMessage {
        public TextContent content;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentMessage extends OriginalMessage {
        public TextContent content;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileMessage extends OriginalMessage {
        public FileContent content;
        public int commentCount;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        public String body;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StickerMessage extends OriginalMessage {
        public StickerContent content;
        public int version;
    }



    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileContent {
        public String title;
        public String name;
        public String type;
        public String icon;
        public String serverUrl;
        public String filterType;
        public String fileUrl;
        public String ext;
        public int size;
        public ThumbnailUrls extraInfo;

        @Override
        public String toString() {
            return "FileContent{" +
                    "title='" + title + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", icon='" + icon + '\'' +
                    ", serverUrl='" + serverUrl + '\'' +
                    ", filterType='" + filterType + '\'' +
                    ", fileUrl='" + fileUrl + '\'' +
                    ", ext='" + ext + '\'' +
                    ", size=" + size +
                    ", extraInfo=" + extraInfo +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbnailUrls {
        public String smallThumbnailUrl;
        public String mediumThumbnailUrl;
        public String largeThumbnailUrl;

        @Override
        public String toString() {
            return "ThumbnailUrls{" +
                    "smallThumbnailUrl='" + smallThumbnailUrl + '\'' +
                    ", mediumThumbnailUrl='" + mediumThumbnailUrl + '\'' +
                    ", largeThumbnailUrl='" + largeThumbnailUrl + '\'' +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateEvent extends EventInfo {

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "entityType")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = PublicCreateInfo.class, name = "channel"),
                @JsonSubTypes.Type(value = PrivateCreateInfo.class, name = "privateGroup")})
        public CreateInfo createInfo;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InviteEvent extends EventInfo {
        public int invitorId;
        public List<Integer> inviteUsers;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeaveEvent extends EventInfo {

    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JoinEvent extends EventInfo {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class CreateInfo {
    }

    public static class PublicCreateInfo extends CreateInfo {
        @JsonProperty("ch_creatorId")
        public int creatorId;
        @JsonProperty("ch_createTime")
        public Date createTime;
        @JsonProperty("ch_isDefault")
        public boolean isDefault;
        @JsonProperty("ch_members")
        public List<Integer> members;
    }

    public static class PrivateCreateInfo extends CreateInfo {
        @JsonProperty("pg_creatorId")
        public int creatorId;
        @JsonProperty("pg_createTime")
        public Date createTime;
        @JsonProperty("pg_isDefault")
        public boolean isDefault;
        @JsonProperty("pg_members")
        public List<Integer> members;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class StickerContent {
        public int groupId;
        public String stickerId;
        public String url;
    }
}
