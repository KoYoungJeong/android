package com.tosslab.toss.app.lists;

import com.tosslab.toss.app.TossConstants;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.FormatConverter;

import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class MessageItem {
    private final Logger log = Logger.getLogger(MessageItem.class);

    public static final int TYPE_STRING = 0;
    public static final int TYPE_IMAGE  = 1;
    public static final int TYPE_COMMENT  = 2;

    public static final int TYPE_FILE  = 10;

    private ResMessages.Link mLink;

    public MessageItem(ResMessages.Link link) {
        mLink = link;
    }

    public int getId() {
        return mLink.message.id;
    }

    public int getFeedbackId() {
        return mLink.message.feedback;
    }

    public String getUserNickName() {
        return mLink.message.writer.u_firstName + " " + mLink.message.writer.u_lastName;
    }

    public int getUserId() {
        return mLink.message.writer.id;
    }

    public String getUserProfileUrl() {
        return TossConstants.SERVICE_ROOT_URL + mLink.message.writer.u_photoUrl;
    }

    public int getContentType() {
        ResMessages.OriginalMessage message = mLink.message;
        if (message instanceof ResMessages.TextMessage) {
            return TYPE_STRING;
        } else if (message instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage)message).content.type;
            log.debug("fileType : " + fileType);
            if (fileType == null || fileType.equals("null")) {
                return TYPE_FILE;
            }
            if (fileType.startsWith("image")) {
                return TYPE_IMAGE;
            } else {
                return TYPE_FILE;
            }
        } else if (message instanceof ResMessages.CommentMessage) {
            return TYPE_COMMENT;
        }
        return TYPE_STRING;
    }

    public String getContentString() {
        if (mLink.message instanceof ResMessages.TextMessage) {
            return ((ResMessages.TextMessage)mLink.message).content.body;
        } else if (mLink.message instanceof ResMessages.CommentMessage) {
            return ((ResMessages.CommentMessage)mLink.message).content.body;
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

    public String getContentFileName() {
        if (mLink.message instanceof ResMessages.FileMessage) {
            return ((ResMessages.FileMessage)mLink.message).content.name;
        }
        return null;
    }

    public String getContentFileType() {
        if (mLink.message instanceof ResMessages.FileMessage) {
            return ((ResMessages.FileMessage)mLink.message).content.type;
        }
        return null;
    }

    public String getContentFileSize() {
        if (mLink.message instanceof ResMessages.FileMessage) {
            int byteSize = ((ResMessages.FileMessage)mLink.message).content.size;
            return FormatConverter.formatFileSize(byteSize);
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
