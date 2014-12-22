package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 18..
 * CDP 메시지 리스트 획득의 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMessages {
    public int lastLinkId;
    public int numOfPage;
    public int firstIdOfReceivedList;
    public boolean isFirst;
    public int messageCount;
    public List<Link> messages;

    @Override
    public String toString() {
        return "ResMessages{" +
                "lastLinkId=" + lastLinkId +
                ", numOfPage=" + numOfPage +
                ", firstIdOfReceivedList=" + firstIdOfReceivedList +
                ", isFirst=" + isFirst +
                ", messageCount=" + messageCount +
                ", messages=" + messages +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Link {
        public int id;
        public int teamId;
        public ResLeftSideMenu.Entity fromEntity;
        public Date time;
        public int messageId;
        public String status;
        public int feedbackId;
        public Info info;
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
        public int invitorId;
        public List<Integer> inviteUsers;
        public String eventType;

        @Override
        public String toString() {
            return "Info{" +
                    "invitorId=" + invitorId +
                    ", inviteUsers=" + inviteUsers +
                    ", eventType='" + eventType + '\'' +
                    '}';
        }
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
            @JsonSubTypes.Type(value = CommentMessage.class, name = "comment")})
    public static class OriginalMessage {
        public int id;
        public int teamId;
        public ResLeftSideMenu.User writer;
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
                    ", writer=" + writer +
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
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        public String body;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileContent {
        public String title;
        public String name;
        public String type;
        public String serverUrl;
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
                    ", serverUrl='" + serverUrl + '\'' +
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

}
