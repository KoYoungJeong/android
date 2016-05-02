package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.IntegrationBotViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.jandi.JandiBotViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

/*
 소스 추적은 getContentType 부터 따라가면 쉽게 파악할 수 있음.
 */
public class BodyViewFactory {

    public static BodyViewHolder createViewHolder(int viewType) {

        BaseViewHolderBuilder builder = new EmptyViewHolder.Builder();

        // Setting View TYPE
        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_NORMAL_MESSAGE)) {
            builder = new MessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_STICKER_MESSAGE)) {
            builder = new StickerMessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_IMAGE_MESSAGE)) {
            builder = new ImageMessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_FILE_MESSAGE)) {
            builder = new FileMessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_STICKER_COMMENT)) {
            builder = new StickerCommentViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_MESSAGE_COMMENT)) {
            builder = new CommentViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_DUMMY_NORMAL_MESSAGE)) {
            builder = new DummyMessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_DUMMY_STICKER)) {
            builder = new DummyMessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_EVENT_MESSAGE)) {
            builder = new EventMessageViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_JANDI_BOT_MESSAGE)) {
            builder = new JandiBotViewHolder.Builder();
        } else if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_VIEW_INTEGRATION_BOT_MESSAGE)) {
            builder = new IntegrationBotViewHolder.Builder();
        }

        // Setting Option
        if (!TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_PURE)) {
            builder.setHasUserProfile(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_ONLY_BADGE)) {
            builder.setHasOnlyBadge(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
            builder.setHasBottomMargin(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_COMMENT_FILE_INFO)) {
            builder.setHasFileInfoView(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL)) {
            builder.setHasCommentBubbleTail(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_COMMENT_VIEW_ALL)) {
            builder.setHasViewAllComment(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE)) {
            builder.setHasNestedProfile(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER)) {
            builder.setHasSemiDivider(true);
        }

        if (TypeUtil.hasTypeElement(viewType, TypeUtil.TYPE_OPTION_HAS_FLAT_TOP)) {
            builder.setHasFlatTop(true);
        }

        return builder.build();

    }

    // first step
    public static int getContentType(ResMessages.Link previousLink,
                                     ResMessages.Link currentLink,
                                     ResMessages.Link nextLink) {
        int type = TypeUtil.TYPE_EMPTY;
        if (isEventMessage(currentLink)) {
            type = getEventMessageType(currentLink, nextLink);
        } else if (isTextMessage(currentLink)) {
            type = getNormalMessageType(previousLink, currentLink, nextLink);
        } else if (isStickerMessage(currentLink)) {
            type = getStickerMessageType(previousLink, currentLink, nextLink);
        } else if (isFileMessage(currentLink)) {
            type = getFileMessageType(currentLink, nextLink);
        } else if (isCommentMessage(currentLink)) {
            type = getCommentMessageType(previousLink, currentLink, nextLink);
        } else if (isCommentStickerMessage(currentLink)) {
            type = getCommentMessageType(previousLink, currentLink, nextLink);
        }
        return type;
    }

    private static int getEventMessageType(ResMessages.Link currentLink,
                                           ResMessages.Link nextLink) {
        int type = TypeUtil.TYPE_VIEW_EVENT_MESSAGE;
        if (isNextLinkSerialEventMessage(currentLink, nextLink)) {
            return type;
        }
        return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static boolean isNextLinkSerialEventMessage(ResMessages.Link currentLink,
                                                        ResMessages.Link nextLink) {
        return nextLink != null
                && isSameDay(currentLink, nextLink)
                && isEventMessage(nextLink);
    }

    private static boolean isEventMessage(ResMessages.Link currentLink) {
        return TextUtils.equals(currentLink.status, "event");
    }

    private static int getNormalMessageType(ResMessages.Link previousLink,
                                            ResMessages.Link currentLink,
                                            ResMessages.Link nextLink) {
        int type = TypeUtil.TYPE_VIEW_NORMAL_MESSAGE;

        if (isPureMessage(previousLink, currentLink)) {
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_PURE);
        }

        if (isDummyMessage(currentLink)) {
            type = TypeUtil.TYPE_VIEW_DUMMY_NORMAL_MESSAGE;
        } else if (isJandiBotMessage(currentLink)) {
            type = TypeUtil.TYPE_VIEW_JANDI_BOT_MESSAGE;
            return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);
        } else if (isIntegrationBotMessage(currentLink)) {
            type = TypeUtil.TYPE_VIEW_INTEGRATION_BOT_MESSAGE;
            return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);
        }

        if (isNextMessageSameWriterAndSameTime(currentLink, nextLink)) {
            // Next Link와 같은 작성자인데 시간이 같다면 시간 생략
            return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_ONLY_BADGE);
        } else if (isNextLinkSameWriterAndCloseTime(currentLink, nextLink)) {
            // Next Link와 같은 작성자인데 시간이 근접하다면 다음 메세지는 Pure 메세지 - 마진이 없음
            return type;
        }

        return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static boolean hasNextMessage(ResMessages.Link nextLink) {
        return nextLink != null && nextLink.message != null;
    }

    private static int getStickerMessageType(ResMessages.Link previousLink,
                                             ResMessages.Link currentLink,
                                             ResMessages.Link nextLink) {
        int type = TypeUtil.TYPE_VIEW_STICKER_MESSAGE;

        if (isPureMessage(previousLink, currentLink)) {
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_PURE);
        }

        if (isDummyMessage(currentLink)) {
            type = TypeUtil.TYPE_VIEW_DUMMY_STICKER;
        }

        if (isNextMessageSameWriterAndSameTime(currentLink, nextLink)) {
            // Next Link와 같은 작성자인데 시간이 같다면 시간 생략
            return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_ONLY_BADGE);
        } else if (isNextLinkSameWriterAndCloseTime(currentLink, nextLink)) {
            // Next Link와 같은 작성자인데 시간이 근접하다면 다음 메세지는 Pure 메세지 - 마진이 없음
            return type;
        }

        return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static boolean isNextMessageSameWriterAndSameTime(ResMessages.Link currentLink,
                                                              ResMessages.Link nextLink) {
        return hasNextMessage(nextLink) &&
                DateComparatorUtil.isSameTime(
                        currentLink.message.createTime, nextLink.message.createTime) &&
                isSameWriter(currentLink.message, nextLink.message) &&
                (isTextMessage(nextLink) || isStickerMessage(nextLink));
    }

    private static boolean isNextLinkSameWriterAndCloseTime(ResMessages.Link currentLink,
                                                            ResMessages.Link nextLink) {
        return hasNextMessage(nextLink) &&
                DateComparatorUtil.isSince5min(
                        nextLink.message.createTime, currentLink.message.createTime) &&
                isSameWriter(currentLink.message, nextLink.message) &&
                (isTextMessage(nextLink) || isStickerMessage(nextLink));
    }


    private static int getCommentMessageType(ResMessages.Link previousLink,
                                             ResMessages.Link currentLink,
                                             ResMessages.Link nextLink) {
        int type;

        if (isCommentStickerMessage(currentLink)) {
            type = TypeUtil.TYPE_VIEW_STICKER_COMMENT;
        } else {
            type = TypeUtil.TYPE_VIEW_MESSAGE_COMMENT;
        }

        if (isPreviousLinkFeedbackOrFile(previousLink, currentLink.feedbackId)) {
            // 1. previous Link가 같은 파일의 커맨트 이거나 파일일 때
            if (isFileMessage(previousLink)) {
                // 2. previous Link가 파일 메세지 일때 파일 정보 없이 Tail/Profile 이 나와야됨
                type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL);
                type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
                if (!isSameDay(previousLink, currentLink)) {
                    // 3. 이전 링크가 날짜가 다르면 파일 정보 추가
                    type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_FILE_INFO);
                    type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_VIEW_ALL);
                    type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_FLAT_TOP);
                }
            } else {
                // 2. previous Link가 Comment 메세지 일때
                type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_FLAT_TOP);
                if (isSameWriter(previousLink.message, currentLink.message)) {
                    // 3. 이전 comment 작성자가 같은 사람 일때
                    if (!isSameDay(previousLink, currentLink)) {
                        // 4. 날짜가 다르다면 파일 정보를 추가해야 됨
                        type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_FILE_INFO);
                        type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL);
                        type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_VIEW_ALL);
                        type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
                    }
                } else {
                    // 3. 이전 comment 작성자가 다른 사람 일때 프로필이 들어가야 함
                    type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
                }
            }
        } else {
            // 1. previous Link가 같은 파일의 커맨트 이거나 파일이 아닐 때
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_FILE_INFO
            );
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL);
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_VIEW_ALL);
            // view all이 있기 때문에 윗면이 라운드가 아니라 평평해야한다..............
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_FLAT_TOP);
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
        }

        if (isNextCommentSameWriterAndSameTime(currentLink, nextLink)) {
            // Next Link와 같은 작성자인데 시간이 같다면 시간 생략
            type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_ONLY_BADGE);
        }

        return getCommentBottomType(currentLink, nextLink, type);
    }

    private static int getCommentBottomType(ResMessages.Link currentLink,
                                            ResMessages.Link nextLink,
                                            int type) {

        if (isSameFeedbackComment(currentLink, nextLink)) {
            // 다음 커멘트 링크가 연속되어야 한다면
            if (isSameWriter(currentLink.message, nextLink.message)) {
                // 다음 Link의 작성자가 같다면 Semi Divider
                type = TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER);
                return type;
            } else {
                // 다음 Link의 작성자가 다르다면 Normal DIVIDER -> Default
                return type;
            }
        }

        // 나머지
        return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static boolean isNextCommentSameWriterAndSameTime(ResMessages.Link currentLink,
                                                              ResMessages.Link nextLink) {
        return hasNextMessage(nextLink) &&
                DateComparatorUtil.isSameTime(
                        currentLink.message.createTime, nextLink.message.createTime) &&
                isSameWriter(currentLink.message, nextLink.message) &&
                (isCommentMessage(nextLink) || isCommentStickerMessage(nextLink));
    }

    private static boolean isSameFeedbackComment(ResMessages.Link currentLink, ResMessages.Link nextLink) {
        return (hasNextMessage(nextLink))
                && (isCommentMessage(nextLink) || isCommentStickerMessage(nextLink))
                && currentLink.feedbackId == nextLink.feedbackId //피드백 아이디도 같다면
                && isSameDay(currentLink, nextLink);
    }

    private static boolean isPreviousLinkFeedbackOrFile(ResMessages.Link previousLink,
                                                        long messageFeedbackId) {
        boolean isFeedbackOrFile = false;
        if (previousLink != null) {
            isFeedbackOrFile =
                    (messageFeedbackId == previousLink.messageId) ||
                            (messageFeedbackId == previousLink.feedbackId);
        }
        return isFeedbackOrFile;
    }

    private static int getFileMessageType(ResMessages.Link currentLink, ResMessages.Link nextLink) {

        boolean isImage = isImageFileMessage(currentLink);
        int type;

        if (isImage) {
            type = TypeUtil.TYPE_VIEW_IMAGE_MESSAGE;
        } else {
            type = TypeUtil.TYPE_VIEW_FILE_MESSAGE;
        }

        if (hasNextLinkComment(currentLink, nextLink)) {
            return type;
        }

        return TypeUtil.addType(type, TypeUtil.TYPE_OPTION_HAS_BOTTOM_MARGIN);

    }

    private static boolean hasNextLinkComment(ResMessages.Link currentLink,
                                              ResMessages.Link nextLink) {
        return nextLink != null &&
                isSameDay(currentLink, nextLink) &&
                isCommentMessage(nextLink) &&
                nextLink.feedbackId == currentLink.messageId;
    }

    private static boolean isImageFileMessage(ResMessages.Link currentLink) {

        if (!isFileMessage(currentLink)) {
            return false;
        }
        boolean isSharedFile = false;
        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) currentLink.message;
        Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = fileMessage.shareEntities;

        // ArrayList로 나오는 경우 아직 DB에 기록되지 않은 경우 - object가 자동갱신되지 않는 문제 해결
        if (shareEntities instanceof ArrayList) {
            ResMessages.FileMessage file =
                    MessageRepository.getRepository().getFileMessage(currentLink.message.id);
            shareEntities = file != null ? file.shareEntities : shareEntities;
        }

        for (ResMessages.OriginalMessage.IntegerWrapper entity : shareEntities) {
            if (entity.getShareEntity() == currentLink.roomId) {
                isSharedFile = true;
            }
        }

        String fileType = fileMessage.content.icon;

        return !TextUtils.isEmpty(fileType)
                && fileType.startsWith("image")
                && SourceTypeUtil.getSourceType(fileMessage.content.serverUrl) == MimeTypeUtil.SourceType.S3
                && isSharedFile
                && !TextUtils.equals(currentLink.message.status, "archived");

    }

    private static boolean isTextMessage(ResMessages.Link link) {

        if (link == null) {
            return false;
        }

        return link.message instanceof ResMessages.TextMessage;

    }

    private static boolean isCommentMessage(ResMessages.Link link) {

        if (link == null) {
            return false;
        }

        return link.message instanceof ResMessages.CommentMessage;

    }

    private static boolean isFileMessage(ResMessages.Link link) {

        if (link == null) {
            return false;
        }

        return link.message instanceof ResMessages.FileMessage;

    }

    private static boolean isStickerMessage(ResMessages.Link link) {

        if (link == null) {
            return false;
        }

        return link.message instanceof ResMessages.StickerMessage;
    }

    private static boolean isCommentStickerMessage(ResMessages.Link link) {

        if (link == null) {
            return false;
        }

        return link.message instanceof ResMessages.CommentStickerMessage;

    }

    private static boolean isDummyMessage(ResMessages.Link link) {
        return link instanceof DummyMessageLink;
    }

    private static boolean isJandiBotMessage(ResMessages.Link link) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(link.message.writerId);
        boolean isBot = entity instanceof BotEntity;
        if (isBot && TextUtils.equals(((BotEntity) entity).getBotType(), "jandi_bot")) {
            return true;
        }
        return false;
    }

    private static boolean isIntegrationBotMessage(ResMessages.Link link) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(link.message.writerId);
        boolean isBot = entity instanceof BotEntity;
        if (isBot && !TextUtils.equals(((BotEntity) entity).getBotType(), "jandi_bot")) {
            return true;
        }
        return false;
    }

    private static boolean isPureMessage(ResMessages.Link previousLink, ResMessages.Link currentLink) {
        return (isTextMessage(previousLink) || isStickerMessage(previousLink))
                && isSameWriter(previousLink.message, currentLink.message)
                && DateComparatorUtil.isSince5min(currentLink.message.createTime, previousLink.message.createTime)
                && isSameDay(currentLink, previousLink);
    }

    private static boolean isSameWriter(ResMessages.OriginalMessage previousMessage, ResMessages.OriginalMessage currentMessage) {
        return currentMessage.writerId == previousMessage.writerId;
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
        public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        }

        @Override
        public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
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

        public static class Builder extends BaseViewHolderBuilder {
            @Override
            public BodyViewHolder build() {
                return new EmptyViewHolder();
            }
        }

    }

}