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
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class BodyViewFactory {

    // Flag 타입은 int 범위 상 총 31개 까지만 가능함.
    public static final int TYPE_VIEW_NORMAL_MESSAGE = 1 << 1;
    public static final int TYPE_VIEW_STICKER_MESSAGE = 1 << 2;
    public static final int TYPE_VIEW_IMAGE_MESSAGE = 1 << 3;
    public static final int TYPE_VIEW_FILE_MESSAGE = 1 << 4;
    public static final int TYPE_VIEW_STICKER_COMMENT = 1 << 5;
    public static final int TYPE_VIEW_MESSAGE_COMMENT = 1 << 6;
    public static final int TYPE_VIEW_DUMMY_NORMAL_MESSAGE = 1 << 7;
    public static final int TYPE_VIEW_DUMMY_STICKER = 1 << 8;
    public static final int TYPE_VIEW_EVENT_MESSAGE = 1 << 9;
    public static final int TYPE_VIEW_JANDI_BOT_MESSAGE = 1 << 10;
    public static final int TYPE_VIEW_INTEGRATION_BOT_MESSAGE = 1 << 11;
    public static final int TYPE_EMPTY = 1 << 12;

    public static final int TYPE_OPTION_PURE = 1 << 20;
    public static final int TYPE_OPTION_HAS_ONLY_BADGE = 1 << 21;
    public static final int TYPE_OPTION_HAS_BOTTOM_MARGIN = 1 << 22;
    public static final int TYPE_OPTION_HAS_COMMENT_FILE_INFO = 1 << 23;
    public static final int TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL = 1 << 24;
    public static final int TYPE_OPTION_HAS_COMMENT_VIEW_ALL = 1 << 25;
    public static final int TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE = 1 << 26;
    public static final int TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER = 1 << 27;

    // 인자 viewType에 flagType의 요소가 포함되어 있는지 검사
    // ex) hasSubsetViewType(viewType, TYPE_VIEW_EVENT_MESSAGE) -> event 타입의 메세지
    private static boolean hasSubsetViewType(int viewType, int flagType) {
        if ((viewType & flagType) > 0) {
            return true;
        }
        return false;
    }

    // viewType에 flagType을 추가
    private static int addViewType(int viewType, int flagType) {
        return viewType | flagType;
    }

    public static BodyViewHolder createViewHolder(int viewType) {

        if (hasSubsetViewType(viewType, TYPE_VIEW_NORMAL_MESSAGE)
                || hasSubsetViewType(viewType, TYPE_VIEW_STICKER_MESSAGE)) {

            MessageViewHolder.Builder builder =
                    new MessageViewHolder.Builder();

            if (!hasSubsetViewType(viewType, TYPE_OPTION_PURE)) {
                builder.setHasUserProfile(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_ONLY_BADGE)) {
                builder.setHasOnlyBadge(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_IMAGE_MESSAGE)) {

            ImageMessageViewHolder.Builder builder =
                    new ImageMessageViewHolder.Builder();

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_FILE_MESSAGE)) {

            FileMessageViewHolder.Builder builder =
                    new FileMessageViewHolder.Builder();

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_STICKER_COMMENT)) {

            StickerCommentViewHolder.Builder builder =
                    new StickerCommentViewHolder.Builder();

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_FILE_INFO)) {
                builder.setHasFileInfoView(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL)) {
                builder.setHasCommentBubbleTail(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_VIEW_ALL)) {
                builder.setHasViewAllComment(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE)) {
                builder.setHasNestedProfile(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER)) {
                builder.setHasSemiDivider(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_MESSAGE_COMMENT)) {

            CommentViewHolder.Builder builder =
                    new CommentViewHolder.Builder();

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_FILE_INFO)) {
                builder.setHasFileInfoView(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL)) {
                builder.setHasCommentBubbleTail(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_VIEW_ALL)) {
                builder.setHasViewAllComment(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE)) {
                builder.setHasNestedProfile(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER)) {
                builder.setHasSemiDivider(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_DUMMY_NORMAL_MESSAGE)
                || hasSubsetViewType(viewType, TYPE_VIEW_DUMMY_STICKER)) {

            DummyMessageViewHolder.Builder builder =
                    new DummyMessageViewHolder.Builder();

            if (!hasSubsetViewType(viewType, TYPE_OPTION_PURE)) {
                builder.setHasProfile(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_EVENT_MESSAGE)) {

            EventMessageViewHolder.Builder builder =
                    new EventMessageViewHolder.Builder();

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();

        } else if (hasSubsetViewType(viewType, TYPE_VIEW_JANDI_BOT_MESSAGE)) {

            JandiBotViewHolder.Builder builder =
                    new JandiBotViewHolder.Builder();


            if (!hasSubsetViewType(viewType, TYPE_OPTION_PURE)) {
                builder.setHasBotProfile(true);
            }

            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }

            return builder.build();


        } else if (hasSubsetViewType(viewType, TYPE_VIEW_INTEGRATION_BOT_MESSAGE)) {

            IntegrationBotViewHolder.Builder builder =
                    new IntegrationBotViewHolder.Builder();

            if (!hasSubsetViewType(viewType, TYPE_OPTION_PURE)) {
                builder.setHasBotProfile(true);
            }
            if (hasSubsetViewType(viewType, TYPE_OPTION_HAS_BOTTOM_MARGIN)) {
                builder.setHasBottomMargin(true);
            }
            return builder.build();

        }

        return new EmptyViewHolder();

    }

    public static int getContentType(ResMessages.Link previousLink,
                                     ResMessages.Link currentLink,
                                     ResMessages.Link nextLink) {

        int type = TYPE_EMPTY;

        if (isEventMessage(currentLink)) {
            type = TYPE_VIEW_EVENT_MESSAGE;
            type = getEventMessageType(currentLink, nextLink, type);
        } else if (isTextMessage(currentLink)) {
            type = TYPE_VIEW_NORMAL_MESSAGE;
            type = getNormalMessageType(currentLink, nextLink, type);
            if (isPureMessage(previousLink, currentLink)) {
                type = addViewType(type, TYPE_OPTION_PURE);
            }
        } else if (isStickerMessage(currentLink)) {
            type = TYPE_VIEW_STICKER_MESSAGE;
            type = getStickerMessageType(currentLink, nextLink, type);
            if (isPureMessage(previousLink, currentLink)) {
                type = addViewType(type, TYPE_OPTION_PURE);
            }
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
                                           ResMessages.Link nextLink, int type) {
        if (isNextLinkSerialEventMessage(currentLink, nextLink)) {
            return type;
        }
        return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static boolean isNextLinkSerialEventMessage(ResMessages.Link currentLink, ResMessages.Link nextLink) {
        return nextLink != null
                && isSameDay(currentLink, nextLink)
                && isEventMessage(nextLink);
    }

    private static boolean isEventMessage(ResMessages.Link currentLink) {
        return TextUtils.equals(currentLink.status, "event");
    }

    private static int getNormalMessageType(
            ResMessages.Link currentLink, ResMessages.Link nextLink, int type) {
        if (isDummyMessage(currentLink)) {
            type = TYPE_VIEW_DUMMY_NORMAL_MESSAGE;
        } else {
            if (isJandiBotMessage(currentLink)) {
                type = TYPE_VIEW_JANDI_BOT_MESSAGE;
                return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
            } else if (isIntegrationBotMessage(currentLink)) {
                type = TYPE_VIEW_INTEGRATION_BOT_MESSAGE;
                return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
            }
        }

        if (nextLink == null || nextLink.message == null) {
            return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
        }

        if (hasOnlyBadgeFromNextLink(currentLink, nextLink)) {
            return addViewType(type, TYPE_OPTION_HAS_ONLY_BADGE);
        } else {
            if (isPureFromNextLink(currentLink, nextLink)) {
                return type;
            }
        }

        return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static int getStickerMessageType(
            ResMessages.Link currentLink, ResMessages.Link nextLink, int type) {

        if (isDummyMessage(currentLink)) {
            type = TYPE_VIEW_DUMMY_STICKER;
        }

        if (nextLink == null || nextLink.message == null) {
            return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
        }
        if (hasOnlyBadgeFromNextLink(currentLink, nextLink)) {
            return addViewType(type, TYPE_OPTION_HAS_ONLY_BADGE);
        } else {
            if (isPureFromNextLink(currentLink, nextLink)) {
                return type;
            }
        }
        return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }


    private static boolean hasOnlyBadgeFromNextLink(ResMessages.Link currentLink, ResMessages.Link nextLink) {
        return DateComparatorUtil.isSameTime(
                currentLink.message.createTime, nextLink.message.createTime) &&
                isSameWriter(currentLink.message, nextLink.message)
                && (isTextMessage(nextLink) || isStickerMessage(nextLink));
    }

    private static boolean isPureFromNextLink(ResMessages.Link currentLink, ResMessages.Link nextLink) {
        return isSameWriter(currentLink.message, nextLink.message) &&
                (isTextMessage(nextLink) || isStickerMessage(nextLink)) &&
                DateComparatorUtil.isSince5min(
                        nextLink.message.createTime, currentLink.message.createTime);
    }

    private static int getCommentMessageType(ResMessages.Link previousLink,
                                             ResMessages.Link currentLink,
                                             ResMessages.Link nextLink) {
        int type;

        if (isCommentStickerMessage(currentLink)) {
            type = TYPE_VIEW_STICKER_COMMENT;
        } else {
            type = TYPE_VIEW_MESSAGE_COMMENT;
        }

        if (isPreviousLinkFeedbackOrFile(previousLink, currentLink.feedbackId)) { // 1. previous Link가 같은 파일의 커맨트 이거나 파일일 때
            if (isFileMessage(previousLink)) { // 2. previous Link가 파일 메세지 일때 파일 정보 없이 TAIL/PROFILE이 나와야됨
                type = addViewType(type, TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL);
                type = addViewType(type, TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
                if (!isSameDay(previousLink, currentLink)) { // 3. 이전 링크가 날짜가 다르면 파일 정보 추가
                    type = addViewType(type, TYPE_OPTION_HAS_COMMENT_FILE_INFO);
                    type = addViewType(type, TYPE_OPTION_HAS_COMMENT_VIEW_ALL);
                }
            } else { // 2. previous Link가 Comment 메세지 일때
                if (isSameWriter(previousLink.message, currentLink.message)) { // 3. 이전 comment 작성자가 같은 사람 일때
                    if (!isSameDay(previousLink, currentLink)) { // 4. 이전 링크와 같은 날이라면 프로필이 없는 Pure
                        // 4. 날짜가 다르다면 파일 정보를 추가해야 됨
                        type = addViewType(type, TYPE_OPTION_HAS_COMMENT_FILE_INFO);
                        type = addViewType(type, TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL);
                        type = addViewType(type, TYPE_OPTION_HAS_COMMENT_VIEW_ALL);
                        type = addViewType(type, TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
                    }
                } else { // 3. 이전 comment 작성자가 다른 사람 일때 프로필이 들어가야 함
                    type = addViewType(type, TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
                }
            }
        } else { // 1. previous Link가 같은 파일의 커맨트 이거나 파일이 아닐 때
            type = addViewType(type, TYPE_OPTION_HAS_COMMENT_FILE_INFO);
            type = addViewType(type, TYPE_OPTION_HAS_COMMENT_BUBBLE_TAIL);
            type = addViewType(type, TYPE_OPTION_HAS_COMMENT_VIEW_ALL);
            type = addViewType(type, TYPE_OPTION_HAS_COMMENT_NESTED_PROFILE);
        }

        return getCommentBottomType(currentLink, nextLink, type);

    }

    private static int getCommentBottomType(ResMessages.Link currentLink,
                                            ResMessages.Link nextLink, int type) {

        if (isSameFeedbackComment(currentLink, nextLink)) { // 다음 커멘트 링크가 있다면
            if (isSameWriter(currentLink.message, nextLink.message)) { // 다음 Link의 작성자가 같다면
                type = addViewType(type, TYPE_OPTION_HAS_COMMENT_SEMI_DIVIDER);
                return type;
            } else { // 다음 Link의 작성자가 다르다면
                return type;
            }
        }

        // 나머지
        return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);

    }

    private static boolean isSameFeedbackComment(ResMessages.Link currentLink, ResMessages.Link nextLink) {
        return (nextLink != null && nextLink.message != null)
                && (isCommentMessage(nextLink) || isCommentStickerMessage(nextLink))
                && currentLink.feedbackId == nextLink.feedbackId//피드백 아이디도 같다면
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
            type = TYPE_VIEW_IMAGE_MESSAGE;
        } else {
            type = TYPE_VIEW_FILE_MESSAGE;
        }

        if (hasNextLinkComment(currentLink, nextLink)) {
            return type;
        }

        return addViewType(type, TYPE_OPTION_HAS_BOTTOM_MARGIN);
    }

    private static boolean hasNextLinkComment(ResMessages.Link currentLink, ResMessages.Link nextLink) {
        return nextLink != null &&
                isSameDay(currentLink, nextLink) &&
                isCommentMessage(nextLink);
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
            ResMessages.FileMessage file = MessageRepository.getRepository().getFileMessage(currentLink.message.id);
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

        boolean f = link.message instanceof ResMessages.CommentStickerMessage;
        return f;
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

    }

}
