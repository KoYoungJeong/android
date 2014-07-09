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
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ResMessages {
    public int numOfPage;
    public int firstIdOfReceviedList;
    public boolean isFirst;
    public int messageCount;
    public List<Link> messages;
    public Date responseTime;

    public static class Link {
        public int id;
        public ResLeftSideMenu.Entity fromEntity;
        public Date time;
        public int messageId;
        public String status;
        public int feedbackId;
        public OriginalMessage feedback;
        public OriginalMessage message;
    }

    /**
     * ResLeftSideMenu.User 와 합칠것.
     */
    public static class Writer {
        public int id;
        public String type;
        public String name;
        public String u_photoUrl;
        public String u_firstName;
        public String u_lastName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
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
        public Writer writer;
        public int writerId;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public String status;
        public List<Integer> shareEntities;
        public int permission;
        public int feedbackId;
    }

    public static class TextMessage extends OriginalMessage {
        public TextContent content;
    }

    public static class CommentMessage extends OriginalMessage {
        public TextContent content;
    }

    public static class FileMessage extends OriginalMessage {
        public FileContent content;
    }

    public static class TextContent {
        public String body;
    }

    public static class FileContent {
        public String title;
        public String name;
        public String type;
        public String serverUrl;
        public String fileUrl;
        public int size;
    }


}
