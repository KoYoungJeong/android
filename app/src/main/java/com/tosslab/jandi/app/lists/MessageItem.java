package com.tosslab.jandi.app.lists;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.FormatConverter;

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

    public boolean isDateDivider;
    private Date mCurrentDate;
    public boolean isToday;

    private ResMessages.Link mLink;
    private ResMessages.OriginalMessage mMessage;
    private ResMessages.Writer mWriter;

    public MessageItem(ResMessages.Link message) {
        mLink = message;
        mMessage = mLink.message;
        mWriter = mMessage.writer;
        mCurrentDate = null;
        isDateDivider = false;
    }

    /**
     * 날짜 경계선 용...
     * @param currentDate
     * @param isToday
     */
    public MessageItem(Date currentDate, boolean isToday) {
        mCurrentDate = currentDate;
        isDateDivider = true;
        this.isToday = isToday;
    }

    public int getLinkId() {
        return mLink.id;
    }
    public int getMessageId() {
        return mLink.messageId;
    }

    public int getFeedbackId() {
        return mLink.feedbackId;
    }

    public String getUserNickName() {
        return mWriter.u_firstName + " " + mWriter.u_lastName;
    }

    public int getUserId() {
        return mWriter.id;
    }

    public String getUserProfileUrl() {
        return JandiConstants.SERVICE_ROOT_URL + mWriter.u_photoUrl;
    }

    public int getContentType() {

        if (mMessage instanceof ResMessages.TextMessage) {
            return TYPE_STRING;
        } else if (mMessage instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage) mMessage).content.type;
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
            return ((ResMessages.TextMessage) mMessage).content.body;
        } else if (mMessage instanceof ResMessages.CommentMessage) {
            return ((ResMessages.CommentMessage) mMessage).content.body;
        }
        return null;
    }

    public String getContentUrl() {
        if (mMessage instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage message = (ResMessages.FileMessage) mMessage;
            if (message.content.serverUrl.equals("root")) {
                return JandiConstants.SERVICE_ROOT_URL + message.content.fileUrl;
            }
        }
        return null;
    }

    public String getContentFileName() {
        if (mMessage instanceof ResMessages.FileMessage) {
            return ((ResMessages.FileMessage) mMessage).content.name;
        }
        return null;
    }

    public String getContentFileType() {
        if (mMessage instanceof ResMessages.FileMessage) {
            return ((ResMessages.FileMessage) mMessage).content.type;
        }
        return null;
    }

    public String getContentFileSize() {
        if (mMessage instanceof ResMessages.FileMessage) {
            int byteSize = ((ResMessages.FileMessage) mMessage).content.size;
            return FormatConverter.formatFileSize(byteSize);
        }

        return null;
    }

    public Date getCurrentDateDevider() {
        return mCurrentDate;
    }

    public Date getLinkTime() {
        return mLink.time;
    }
    public Date getCreatTime() {
        return mMessage.createTime;
    }
}
