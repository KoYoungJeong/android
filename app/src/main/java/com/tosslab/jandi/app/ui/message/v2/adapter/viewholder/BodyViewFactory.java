package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.DateComparatorUtil;

import java.util.Calendar;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class BodyViewFactory {

    public static BodyViewHolder createViewHolder(int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case CollapseComment:
                return new CollapseCommentViewHolder();
            case PureComment:
                return new PureCommentViewHolder();
            case FileComment:
                return new FileCommentViewHolder();
            case CollapseStickerComment:
                return new CollapseStickerCommentViewHolder();
            case PureStickerComment:
                return new PureStickerCommentViewHolder();
            case FileStickerComment:
                return new FileStickerCommentViewHolder();
            case PureMessage:
                return new PureMessageViewHolder();
            case Sticker:
                return new StickerViewHolder();
            case PureSticker:
                return new PureStickerViewHolder();
            case File:
                return FileViewHolder.createFileViewHolder();
            case Image:
                return new ImageViewHolder();
            case Dummy:
                return new DummyViewHolder();
            case DummyPure:
                return new DummyPureViewHolder();
            case Event:
                return new EventViewHolder();
            case PureLinkPreviewMessage:
                return new PureLinkPreviewViewHolder();
            case Message:
            default:
                return new MessageViewHolder();
        }
    }

    public static BodyViewHolder.Type getContentType(ResMessages.Link message,
                                                     ResMessages.Link beforeMessage) {
        ResMessages.OriginalMessage currentMessage = message.message;

        if (TextUtils.equals(message.status, "event")) {
            return BodyViewHolder.Type.Event;
        }

        if (currentMessage instanceof ResMessages.TextMessage || currentMessage instanceof ResMessages.StickerMessage) {

            if (beforeMessage != null
                    &&
                    (beforeMessage.message instanceof ResMessages.TextMessage
                            || beforeMessage.message instanceof ResMessages.StickerMessage)
                    && currentMessage.writerId == beforeMessage.message.writerId
                    && DateComparatorUtil.isSince5min(currentMessage.createTime, beforeMessage.message.createTime)
                    && isSameDay(message, beforeMessage)) {
                if (message instanceof DummyMessageLink) {
                    return BodyViewHolder.Type.DummyPure;
                } else {
                    if (!(currentMessage instanceof ResMessages.TextMessage)) {
                        return BodyViewHolder.Type.PureSticker;
                    }

                    boolean hasLinkPreviewBoth = message.hasLinkPreview() && beforeMessage.hasLinkPreview();

                    return hasLinkPreviewBoth ? BodyViewHolder.Type.PureLinkPreviewMessage : BodyViewHolder.Type.PureMessage;
                }
            } else {
                if (message instanceof DummyMessageLink) {
                    return BodyViewHolder.Type.Dummy;
                } else {
                    return currentMessage instanceof ResMessages.TextMessage ? BodyViewHolder.Type.Message : BodyViewHolder.Type.Sticker;
                }
            }

        } else if (currentMessage instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage) currentMessage).content.icon;
            if (TextUtils.isEmpty(fileType) || fileType.equals("null")) {
                return BodyViewHolder.Type.File;
            }
            if (fileType.startsWith("image")
                    && !TextUtils.equals(currentMessage.status, "archived")) {
                return BodyViewHolder.Type.Image;
            } else {
                return BodyViewHolder.Type.File;
            }
        } else if (currentMessage instanceof ResMessages.CommentMessage || currentMessage instanceof ResMessages.CommentStickerMessage) {
            int messageFeedbackId = message.feedbackId;

            boolean isFeedbackMessage = false;

            if (beforeMessage != null) {
                isFeedbackMessage = messageFeedbackId == beforeMessage.messageId
                        || messageFeedbackId == beforeMessage.feedbackId;
            }

            /*
             * Comment는 파일 바로 밑에 달리는 PureComment, CollapseComment
             * 그리고 다른 날짜나 이전 메세지가 해당 파일이나 해당 파일의 Comment가 아닌 경우 FileComment 사용
             *
             * 1. 이전 메세지가 null이 아니여서 comment형태가 될 수 있는 경우
             * 2. 현 메세지의 feedbackId와 이전 메세지의 Id가 같거나
             * 현 메세지의 feedbackId와 이전 메세지의 feedbackId가 같은 경우
             * 3. 같은 날짜에 작성된 경우
             * 1,2,3 모두 해당될때 PureComment나 CollapseComment의 view를 보여준다.
             */
            boolean isStickerMessage = currentMessage instanceof ResMessages.CommentStickerMessage;

            if (beforeMessage != null
                    && isFeedbackMessage
                    && isSameDay(message, beforeMessage)) {

                ResMessages.OriginalMessage beforeOriginalMessage = beforeMessage.message;

                /*
                 * 1. 5분이내에 작성된 경우
                 * 2. 현재 메세지와 이전 메세지의 작성자가 같은 경우
                 * 3. 현재 메세지의 feedbackId와 이전 메세지의 Id가 다른 경우
                 * 1,2,3 모두 해당 할때 CollapseComment
                 */


                if (DateComparatorUtil.isSince5min(currentMessage.createTime, beforeOriginalMessage.createTime)
                        && currentMessage.writerId == beforeOriginalMessage.writerId
                        && messageFeedbackId != beforeMessage.messageId) {
                    return isStickerMessage ? BodyViewHolder.Type.CollapseStickerComment : BodyViewHolder.Type.CollapseComment;
                } else {
                    return isStickerMessage ? BodyViewHolder.Type.PureStickerComment : BodyViewHolder.Type.PureComment;
                }
            } else {
                return isStickerMessage ? BodyViewHolder.Type.FileStickerComment : BodyViewHolder.Type.FileComment;
            }
        }
        return BodyViewHolder.Type.Message;
    }

    private static boolean isSameDay(ResMessages.Link message, ResMessages.Link beforeMessage) {
        if (message == null || beforeMessage == null) {
            return false;
        }

        ResMessages.OriginalMessage beforeOriginalMessage = beforeMessage.message;
        ResMessages.OriginalMessage originalMessage = message.message;

        if (originalMessage.createTime == null || beforeOriginalMessage.createTime == null) {
            return false;
        }

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(originalMessage.createTime);

        Calendar beforeCalendar = Calendar.getInstance();
        beforeCalendar.setTime(beforeOriginalMessage.createTime);

        int messageDay = messageCalendar.get(Calendar.DAY_OF_YEAR);
        int beforeMessageDay = beforeCalendar.get(Calendar.DAY_OF_YEAR);

        return (messageDay == beforeMessageDay);
    }
}
