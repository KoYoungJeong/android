package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.DateComparatorUtil;

import java.util.Calendar;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class BodyViewFactory {

    private final static EmptyViewHolder EMPTY_VIEW_HOLDER = new EmptyViewHolder();

    public static BodyViewHolder createViewHolder(int viewType) {

        BodyViewHolder.Type type = BodyViewHolder.Type.values()[viewType];

        switch (type) {
            case CollapseComment:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new CollapseCommentViewHolder())
                        .build();
            case PureComment:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new PureCommentViewHolder())
                        .build();
            case FileComment:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new FileCommentViewHolder())
                        .build();
            case CollapseStickerComment:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new CollapseStickerCommentViewHolder())
                        .build();
            case PureStickerComment:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new PureStickerCommentViewHolder())
                        .build();
            case FileStickerComment:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new FileStickerCommentViewHolder())
                        .build();
            case CollapseCommentWioutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new CollapseCommentViewHolder())
                        .build();
            case PureCommentWioutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new PureCommentViewHolder())
                        .build();
            case FileCommentWioutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new FileCommentViewHolder())
                        .build();
            case CollapseStickerCommentWioutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new CollapseStickerCommentViewHolder())
                        .build();
            case PureStickerCommentWioutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new PureStickerCommentViewHolder())
                        .build();
            case FileStickerCommentWioutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new FileStickerCommentViewHolder())
                        .build();
            case PureMessage:
                return new PureMessageViewHolder();
            case Sticker:
                return new StickerViewHolder();
            case PureSticker:
                return new PureStickerViewHolder();
            case File:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new FileViewHolder())
                        .build();
            case Image:
                return new Divider.Builder()
                        .divider(true)
                        .bodyViewHolder(new ImageViewHolder())
                        .build();
            case FileWithoutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new FileViewHolder())
                        .build();
            case ImageWithoutDivider:
                return new Divider.Builder()
                        .divider(false)
                        .bodyViewHolder(new ImageViewHolder())
                        .build();
            case Dummy:
                return new DummyViewHolder();
            case DummyPure:
                return new DummyPureViewHolder();
            case Event:
                return new EventViewHolder();
            case PureLinkPreviewMessage:
                return new PureLinkPreviewViewHolder();
            case Message:
                return new MessageViewHolder();
            default:
                return EMPTY_VIEW_HOLDER;
        }
    }

    public static BodyViewHolder.Type getContentType(ResMessages.Link previousLink, ResMessages.Link currentLink,
                                                     ResMessages.Link nextLink) {
        ResMessages.OriginalMessage currentMessage = currentLink.message;

        if (TextUtils.equals(currentLink.status, "event")) {
            return BodyViewHolder.Type.Event;
        }

        if (currentMessage instanceof ResMessages.TextMessage || currentMessage instanceof ResMessages.StickerMessage) {

            if (previousLink != null
                    &&
                    (previousLink.message instanceof ResMessages.TextMessage
                            || previousLink.message instanceof ResMessages.StickerMessage)
                    && currentMessage.writerId == previousLink.message.writerId
                    && DateComparatorUtil.isSince5min(currentMessage.createTime, previousLink.message.createTime)
                    && isSameDay(currentLink, previousLink)) {
                if (currentLink instanceof DummyMessageLink) {
                    return BodyViewHolder.Type.DummyPure;
                } else {
                    if (!(currentMessage instanceof ResMessages.TextMessage)) {
                        return BodyViewHolder.Type.PureSticker;
                    }

                    boolean hasLinkPreviewBoth = currentLink.hasLinkPreview() && previousLink.hasLinkPreview();

                    return hasLinkPreviewBoth ? BodyViewHolder.Type.PureLinkPreviewMessage : BodyViewHolder.Type.PureMessage;
                }
            } else {
                if (currentLink instanceof DummyMessageLink) {
                    return BodyViewHolder.Type.Dummy;
                } else {
                    return currentMessage instanceof ResMessages.TextMessage ? BodyViewHolder.Type.Message : BodyViewHolder.Type.Sticker;
                }
            }

        } else if (currentMessage instanceof ResMessages.FileMessage) {
            String fileType = ((ResMessages.FileMessage) currentMessage).content.icon;

            boolean isImage = !TextUtils.isEmpty(fileType)
                    && fileType.startsWith("image")
                    && !TextUtils.equals(currentMessage.status, "archived");


            BodyViewHolder.Type defaultType;
            if (isImage) {
                defaultType = BodyViewHolder.Type.Image;
            } else {
                defaultType = BodyViewHolder.Type.File;
            }

            boolean notNeedDivider = nextLink != null
                    && isCommentToNext(nextLink)
                    && nextLink.feedbackId == currentMessage.id
                    && currentMessage.writerId == nextLink.message.writerId
                    && DateComparatorUtil.isSince5min(currentMessage.createTime, nextLink.message.createTime)
                    || !isSameDay(currentLink, nextLink);

            if (nextLink == null) {
                notNeedDivider = true;
            }
            if (notNeedDivider) {
                if (isImage) {
                    return BodyViewHolder.Type.ImageWithoutDivider;
                } else {
                    return BodyViewHolder.Type.FileWithoutDivider;
                }
            } else {
                return defaultType;
            }
        } else if (currentMessage instanceof ResMessages.CommentMessage || currentMessage instanceof ResMessages.CommentStickerMessage) {
            int messageFeedbackId = currentLink.feedbackId;

            boolean isFeedbackOrFile = false;

            if (previousLink != null) {
                isFeedbackOrFile = messageFeedbackId == previousLink.messageId
                        || messageFeedbackId == previousLink.feedbackId;
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

            boolean notNeedDivider = nextLink != null
                    && isCommentToNext(nextLink)
                    && nextLink.feedbackId == currentMessage.feedbackId
                    && currentMessage.writerId == nextLink.message.writerId
                    && DateComparatorUtil.isSince5min(currentMessage.createTime, nextLink.message.createTime)
                    || !isSameDay(currentLink, nextLink);

            if (nextLink == null) {
                notNeedDivider = true;
            }

            if (previousLink != null
                    && isFeedbackOrFile
                    && isSameDay(currentLink, previousLink)) {

                ResMessages.OriginalMessage beforeOriginalMessage = previousLink.message;

                /*
                 * 1. 5분이내에 작성된 경우
                 * 2. 현재 메세지와 이전 메세지의 작성자가 같은 경우
                 * 3. 현재 메세지의 feedbackId와 이전 메세지의 Id가 다른 경우
                 * 1,2,3 모두 해당 할때 CollapseComment
                 */


                if (DateComparatorUtil.isSince5min(currentMessage.createTime, beforeOriginalMessage.createTime)
                        && currentMessage.writerId == beforeOriginalMessage.writerId) {
                    if (notNeedDivider) {
                        return isStickerMessage ? BodyViewHolder.Type.CollapseStickerCommentWioutDivider : BodyViewHolder.Type.CollapseCommentWioutDivider;
                    } else {
                        return isStickerMessage ? BodyViewHolder.Type.CollapseStickerComment : BodyViewHolder.Type.CollapseComment;
                    }
                } else {
                    if (notNeedDivider) {
                        return isStickerMessage ? BodyViewHolder.Type.PureStickerCommentWioutDivider : BodyViewHolder.Type.PureCommentWioutDivider;
                    } else {
                        return isStickerMessage ? BodyViewHolder.Type.PureStickerComment : BodyViewHolder.Type.PureComment;
                    }
                }
            } else {
                if (notNeedDivider) {
                    return isStickerMessage ? BodyViewHolder.Type.FileStickerCommentWioutDivider : BodyViewHolder.Type.FileCommentWioutDivider;
                } else {
                    return isStickerMessage ? BodyViewHolder.Type.FileStickerComment : BodyViewHolder.Type.FileComment;
                }
            }
        }
        return BodyViewHolder.Type.Empty;
    }

    private static boolean isCommentToNext(ResMessages.Link nextLink) {
        return nextLink.message instanceof ResMessages.CommentMessage
                || nextLink.message instanceof ResMessages.CommentStickerMessage;
    }

    private static boolean isSameDay(ResMessages.Link message, ResMessages.Link beforeMessage) {
        if (message == null || beforeMessage == null) {
            return false;
        }

        if (message.time == null || beforeMessage.time == null) {
            return false;
        }

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(message.time);

        Calendar beforeCalendar = Calendar.getInstance();
        beforeCalendar.setTime(beforeMessage.time);

        int messageDay = messageCalendar.get(Calendar.DAY_OF_YEAR);
        int beforeMessageDay = beforeCalendar.get(Calendar.DAY_OF_YEAR);

        return (messageDay == beforeMessageDay);
    }

    private static class EmptyViewHolder implements BodyViewHolder {
        @Override
        public void initView(View rootView) {

        }

        @Override
        public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        }

        @Override
        public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {

        }

        @Override
        public int getLayoutId() {
            return R.layout.item_message_empty_v2;
        }

        @Override
        public void setOnItemClickListener(View.OnClickListener itemClickListener) {

        }

        @Override
        public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {

        }
    }
}
