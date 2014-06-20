package com.tosslab.toss.app.navigation;

import com.tosslab.toss.app.TossConstants;
import com.tosslab.toss.app.network.models.ResMessages;

import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class MessageItem {
    public static final int TYPE_STRING = 0;
    public static final int TYPE_IMAGE  = 1;
    public static final int TYPE_COMMENT  = 1;

    private ResMessages.Link mLink;

    public MessageItem(ResMessages.Link link) {
        mLink = link;
    }

    public int getId() {
        return mLink.message.id;
    }

    public String getUserNickName() {
        return mLink.message.writer.u_firstName + " " + mLink.message.writer.u_lastName;
    }

    public String getUserProfileUrl() {
        return TossConstants.SERVICE_ROOT_URL + mLink.message.writer.u_photoUrl;
    }

    public int getContentType() {
        if (mLink.message instanceof ResMessages.TextMessage) {
            return TYPE_STRING;
        } else if (mLink.message instanceof ResMessages.FileMessage) {
            // TODO : 다른 파일들
            return TYPE_IMAGE;
        } else if (mLink.message instanceof ResMessages.CommentMessage) {
            return TYPE_COMMENT;
        }
        return TYPE_STRING;
    }

    public String getContentString() {
        if (mLink.message instanceof ResMessages.TextMessage) {
            return ((ResMessages.TextMessage)mLink.message).content.body;
        }
        return null;
    }

    public String getContentUrl() {
        if (mLink.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage message = (ResMessages.FileMessage)mLink.message;
            if (message.content.serverUrl.equals("root")) {
                return TossConstants.SERVICE_ROOT_URL + message.content.fileUrl;
            }
        }
        return null;
    }

    public Date getTime() {
        return mLink.time;
    }

    public int getWriterId() {
        return mLink.message.writerId;
        // TODO : 본인 ID 와 비교해서 구현할 것
//        if (mLink.message.writer.id == myUserId) {
//            return true;
//        }
//        return false;
    }
}
