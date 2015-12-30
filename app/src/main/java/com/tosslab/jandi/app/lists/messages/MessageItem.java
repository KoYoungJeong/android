package com.tosslab.jandi.app.lists.messages;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.FormatConverter;

import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class MessageItem {

    public static final int TYPE_STRING = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_STICKER = 2;
    public static final int TYPE_STICKER_COMMNET = 3;
    public static final int TYPE_COMMENT = 4;
    public static final int TYPE_FILE = 10;

    public boolean isDateDivider;   // 날짜 경계선인지 여부
    public boolean isNested;        // 상위 메시지 아이템에 종속된 댓글인지의 여부
    public boolean isNestedOfMine;  // 상위 메시지 아이템이 본인의 것인지의 여부
    public boolean isToday;
    private Date mCurrentDate;
    private ResMessages.Link mLink;
    private ResMessages.OriginalMessage mMessage;
    //    private ResLeftSideMenu.User mWriter;

    public MessageItem(ResMessages.Link message) {
        mLink = message;
        mMessage = mLink.message;
        mCurrentDate = null;
        isDateDivider = false;
        isNested = false;
        isNestedOfMine = false;
    }

    /**
     * 날짜 경계선 용...
     *
     * @param currentDate
     * @param isToday
     */
    public MessageItem(Date currentDate, boolean isToday) {
        mCurrentDate = currentDate;
        isDateDivider = true;
        this.isToday = isToday;
        isNested = false;
        isNestedOfMine = false;
    }

    public ResMessages.Link getLink() {
        return mLink;
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

    public String getFeedbackWriterName() {
        return null;
    }

    public String getFeedbackFileName() {
//        if (mLink.feedbackId > 0) {
//            return ((ResMessages.FileMessage) mLink.feedback).content.name;
//        }
        return null;
    }

    public FormattedEntity getUser() {
        return null;
    }

    public String getUserName() {
        return null;
    }

    public int getUserId() {
        return -1;
    }

    public String getUserProfileUrl() {
        return null;
    }

    public int getContentType() {

        if (mMessage instanceof ResMessages.TextMessage) {
            return TYPE_STRING;
        } else if (mMessage instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage) mMessage).content.type;
            if (TextUtils.isEmpty(fileType) || fileType.equals("null")) {
                return TYPE_FILE;
            }
            if (fileType.startsWith("image")) {
                return TYPE_IMAGE;
            } else {
                return TYPE_FILE;
            }
        } else if (mMessage instanceof ResMessages.CommentMessage) {
            return TYPE_COMMENT;
        } else if (mMessage instanceof ResMessages.StickerMessage) {
            return TYPE_STICKER;
        } else if (mMessage instanceof ResMessages.CommentStickerMessage) {
            return TYPE_STICKER_COMMNET;
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
                return JandiConstantsForFlavors.SERVICE_ROOT_URL + message.content.fileUrl;
            }
        }
        return null;
    }

    public String getContentSmallThumbnailUrl() {
        if (mMessage instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage message = (ResMessages.FileMessage) mMessage;
            if (message.content.extraInfo == null) {
                return null;
            }
            return ImageUtil.getFileUrl(message.content.extraInfo.smallThumbnailUrl);
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
            long byteSize = ((ResMessages.FileMessage) mMessage).content.size;
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
