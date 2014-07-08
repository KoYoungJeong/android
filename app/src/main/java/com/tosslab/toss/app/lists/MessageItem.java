package com.tosslab.toss.app.lists;

import com.tosslab.toss.app.JandiConstants;
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

    private ResMessages.OriginalMessage mMessage;
    private Date mTime;

    public MessageItem(ResMessages.OriginalMessage message, Date time) {
        mMessage = message;
        mTime = time;
    }

    public int getId() {
        return mMessage.id;
    }

    public int getFeedbackId() {
        return mMessage.feedbackId;
    }

    public String getUserNickName() {
        return mMessage.writer.u_firstName + " " + mMessage.writer.u_lastName;
    }

    public int getUserId() {
        return mMessage.writer.id;
    }

    public String getUserProfileUrl() {
        return JandiConstants.SERVICE_ROOT_URL + mMessage.writer.u_photoUrl;
    }

    public int getContentType() {

        if (mMessage instanceof ResMessages.TextMessage) {
            return TYPE_STRING;
        } else if (mMessage instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage)mMessage).content.type;
            log.debug("fileType : " + fileType);
            if (fileType == null || fileType.equals("null")) {
                return TYPE_FILE;
            }
            if (fileType.startsWith("image")) {
                return TYPE_IMAGE;
            } else {
                return TYPE_FILE;
            }
        } else if (mMessage instanceof ResMessages.CommentMessage) {
            return TYPE_COMMENT;
        }
        return TYPE_STRING;
    }

    public String getContentString() {
        if (mMessage instanceof ResMessages.TextMessage) {
            return ((ResMessages.TextMessage)mMessage).content.body;
        } else if (mMessage instanceof ResMessages.CommentMessage) {
            return ((ResMessages.CommentMessage)mMessage).content.body;
        }
        return null;
    }

    public String getContentUrl() {
        if (mMessage instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage message = (ResMessages.FileMessage)mMessage;
            if (message.content.serverUrl.equals("root")) {
                return JandiConstants.SERVICE_ROOT_URL + message.content.fileUrl;
            }
        }
        return null;
    }

    public String getContentFileName() {
        if (mMessage instanceof ResMessages.FileMessage) {
            return ((ResMessages.FileMessage)mMessage).content.name;
        }
        return null;
    }

    public String getContentFileType() {
        if (mMessage instanceof ResMessages.FileMessage) {
            return ((ResMessages.FileMessage)mMessage).content.type;
        }
        return null;
    }

    public String getContentFileSize() {
        if (mMessage instanceof ResMessages.FileMessage) {
            int byteSize = ((ResMessages.FileMessage)mMessage).content.size;
            return FormatConverter.formatFileSize(byteSize);
        }

        return null;
    }

    public Date getTime() {
        return mTime;
    }

    public int getWriterId() {
        return mMessage.writerId;
        // TODO : 본인 ID 와 비교해서 구현할 것
//        if (mLink.message.writer.id == myUserId) {
//            return true;
//        }
//        return false;
    }
}
