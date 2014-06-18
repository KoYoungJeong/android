package com.tosslab.toss.app.navigation;

import com.tosslab.toss.app.network.entities.ResMessages;

import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class MessageItem {
    public static final int TYPE_STRING = 0;
    public static final int TYPE_IMAGE  = 1;

    public final int id;
    public final String userProfileUrl;
    public final String userNickName;
    public final Date createTime;
    public final int contentType;
    public final String contentString;

    public MessageItem(ResMessages.Link link) {
        this.id = link.id;
        this.userNickName = link.message.writer.name;
        this.userProfileUrl = link.message.writer.u_photoUrl;
        this.createTime = link.time;

        if (link.message instanceof ResMessages.TextMessage) {
            this.contentType = TYPE_STRING;
            this.contentString = ((ResMessages.TextMessage)link.message).content.body;
        } else {
            // TODO : 다른 파일들
            this.contentType = TYPE_IMAGE;
            this.contentString = "";
        }
    }
}
