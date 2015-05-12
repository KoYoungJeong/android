package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class BodyViewFactory {

    public static BodyViewHolder createViewHolder(int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case PureMessage:
                return new PureMessageViewHolder();
            case File:
                return FileViewHolder.createFileViewHolder();
            case Image:
                return new ImageViewHolder();
            case PureComment:
                return new PureCommentViewHolder();
            case FileComment:
                return new FileCommentViewHolder();
            case Dummy:
                return new DummyViewHolder();
            case DummyPure:
                return new DummyPureViewHolder();
            case Event:
                return new EventViewHolder();
            case Message:
            default:
                return new MessageViewHolder();
        }
    }

    public static int getViewHolderId(int viewType) {
        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case File:
                return R.id.message_file;
            case Image:
                return R.id.message_img;
            case PureComment:
                return R.id.message_cmt_without_file;
            case FileComment:
                return R.id.message_cmt_with_file;
            case Dummy:
                return R.id.message_dummy;
            case Event:
                return R.id.message_event;
            case Message:
            default:
                return R.id.message_msg;
        }
    }

    public static View createItemView(Context context, ViewGroup parent, int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        int layoutId = 0;

        switch (type) {
            case Message:
                break;
            case File:
                break;
            case Image:
                break;
            case PureComment:
                break;
            case FileComment:
                break;
            case Dummy:
                break;
            case Event:
                break;
        }

        return null;
    }

    public static BodyViewHolder.Type getContentType(ResMessages.Link message, ResMessages.Link beforeMessage) {

        if (TextUtils.equals(message.status, "event")) {
            return BodyViewHolder.Type.Event;
        }

        if (message.message instanceof ResMessages.TextMessage) {


            if (beforeMessage != null
                    && beforeMessage.message instanceof ResMessages.TextMessage
                    && message.message.writerId == beforeMessage.message.writerId
                    && isSince5min(message.message.updateTime, beforeMessage.message.updateTime)
                    && isSameDay(message, beforeMessage)) {

                if (message instanceof DummyMessageLink) {
                    return BodyViewHolder.Type.DummyPure;
                } else {
                    return BodyViewHolder.Type.PureMessage;
                }
            } else {
                if (message instanceof DummyMessageLink) {
                    return BodyViewHolder.Type.Dummy;
                } else {
                    return BodyViewHolder.Type.Message;
                }
            }

        } else if (message.message instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage) message.message).content.type;
            if (TextUtils.isEmpty(fileType) || fileType.equals("null")) {
                return BodyViewHolder.Type.File;
            }
            if (fileType.startsWith("image")) {
                return BodyViewHolder.Type.Image;
            } else {
                return BodyViewHolder.Type.File;
            }
        } else if (message.message instanceof ResMessages.CommentMessage) {

            if ((beforeMessage == null ||
                    message.feedbackId == beforeMessage.messageId ||
                    message.feedbackId == beforeMessage.feedbackId) && isSameDay(message, beforeMessage)) {
                return BodyViewHolder.Type.PureComment;
            } else {
                return BodyViewHolder.Type.FileComment;
            }

        }
        return BodyViewHolder.Type.Message;
    }

    private static boolean isSince5min(Date currentMessageTime, Date beforeMessageTime) {

        long beforeTime = beforeMessageTime.getTime();
        long currentTime = currentMessageTime.getTime();

        double diffTime = currentTime - beforeTime;
        if (diffTime / (1000l * 60l * 5) < 1d) {
            return true;
        }

        return false;
    }

    private static boolean isSameDay(ResMessages.Link message, ResMessages.Link beforeMessage) {

        if (message == null || beforeMessage == null) {
            return false;
        }

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(message.message.updateTime);

        Calendar beforeCalendar = Calendar.getInstance();
        beforeCalendar.setTime(beforeMessage.message.updateTime);

        int messageDay = messageCalendar.get(Calendar.DAY_OF_YEAR);
        int beforeMessageDay = beforeCalendar.get(Calendar.DAY_OF_YEAR);

        return (messageDay == beforeMessageDay);
    }
}
