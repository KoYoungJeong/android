package com.tosslab.toss.app.network.entities;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 3..
 */
public class CdpMessages {
    public static class ChannelMessage {
        public int id;
        public int channelId;
        public String writerId;
        public Writer writer;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public Content content;
        public String status;
    }

    public static class PrivateGroupMessage {
        public int id;
        public int privateGroupId;
        public String writerId;
        public Writer writer;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public Content content;
        public String status;

        // TODO : Legacy
        public int nTeamId;
    }

    public static class DirectMessage {
        public int id;
        public String fromUserId;
        public Writer fromUser;
        public String toUserId;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public Content content;
        public String status;

        // TODO : Legacy
        public int teamId;
    }

    public static class Writer {
        public String id;
        public String nickname;
        public String firstName;
        public String lastName;
        public String photoUrl;
    }

//    @JsonTypeInfo(
//            use = JsonTypeInfo.Id.NAME,
//            include = JsonTypeInfo.As.PROPERTY,
//            property = "type")
//    @JsonSubTypes({
//            @JsonSubTypes.Type(value = String.class, name = "string"),
//            @JsonSubTypes.Type(value = Dog.class, name = "dog") })
    public static class Content {
        public int id;
        public Writer writer;
        public String writerId;
        public String title;
        public String name;
        public String type;
        public String serverUrl;
        public String fileUrl;
        public Date commentUpdateTime;
        public List<Comment> comments;
        public Date createTime;
        public Date updateTime;
        public String status;
        public int commentCount;
    }

    public static class Comment {

    }
}
