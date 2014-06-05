package com.tosslab.toss.app.network.entities;

import java.util.Date;

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
        public String content;
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
        public String content;
        public String status;
    }

    public static class DirectMessage {
        public int id;
        public String fromUserId;
        public Writer fromUser;
        public String toUserId;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public String content;
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
}
