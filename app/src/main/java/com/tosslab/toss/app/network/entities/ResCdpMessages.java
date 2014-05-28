package com.tosslab.toss.app.network.entities;

import java.util.Date;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class ResCdpMessages {
    public int numOfPage;
    public int firstIdOfReceviedList;
    public boolean isFirst;
    public int messageCount;
    public List<Message> messages;

    public static class Message {
        public int id;
        public String writerId;
        public Writer writer;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public String content;
    }

    public static class Writer {
        public String id;
        public String nickname;
        public String firstName;
        public String lastName;
        public String photoUrl;
    }
}
