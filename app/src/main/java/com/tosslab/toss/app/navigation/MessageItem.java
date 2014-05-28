package com.tosslab.toss.app.navigation;

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

    public MessageItem(int id, String userNickName, String userProfileUrl, Date createTime,
                       String contentType, String contentString) {
        this.id = id;
        this.userNickName = userNickName;
        this.userProfileUrl = userProfileUrl;
        if (contentType.equals("image")) {
            this.contentType = TYPE_IMAGE;
        } else {
            this.contentType = TYPE_STRING;
        }
        this.createTime = createTime;
        this.contentString = contentString;
    }
}
