package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public interface BodyViewHolder {

    void initView(View rootView);

    void bindData(ResMessages.Link link, long teamId, long roomId, long entityId);

    void setLastReadViewVisible(long currentLinkId, long lastReadLinkId);

    int getLayoutId();

    void setOnItemClickListener(View.OnClickListener itemClickListener);

    void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener);

    enum Type {
        Message, MessageOnlyHasBadge, MessageHasBottomMargin,
        PureMessage, PureMessageOnlyHasBadge, PureMessageHasBottomMargin,
        PureLinkPreviewMessage,
        Sticker, PureSticker,
        File, FileWithoutDivider,
        Image, ImageWithoutDivider,
        FileComment, FileCommentWioutDivider,
        PureComment, PureCommentWioutDivider,
        CollapseComment, CollapseCommentWioutDivider,
        FileStickerComment, FileStickerCommentWioutDivider,
        PureStickerComment, PureStickerCommentWioutDivider,
        CollapseStickerComment, CollapseStickerCommentWioutDivider,
        Dummy, DummyPure, DummyHasBottomMargin, DummyPureHasBottomMargin,
        Empty,
        Event,
        JandiBot, PureJandiBot, CollapseLinkPreviewJandiBot,
        IntegrationBot, StickerOnlyHasBadge, StickerHasBottomMargin, PureStickerHasBottomMargin, PureStickerOnlyHasBadge, EventHasBottomMargin, FileHasBottomMargin, ImageHasBottomMargin, JandiBotHasBottomMargin, JandiBotOnlyHasBadge, PureJandiBotBottomMargin, PureJandiBotOnlyHasBadge, IntegrationBotHasBottomMargin, IntegrationBotOnlyHasBadge, PureIntegrationBotHasBottomMargin, PureIntegrationBotOnlyHasBadge, FileWithComment, FileWithStickerComment, FileWithCommentHasBottomMargin, FileWithStickerCommentHasBottomMargin, FileWithCommentHasSemiDivider, FileWithStickerCommentHasSemiDivider, FileWithCommentHasNormalDivider, FileWithStickerCommentHasNormalDivider, ProfileWithComment, ProfileWithCommentHasBottomMargin, ProfileWithCommentHasSemiDivider, ProfileWithCommentHasNormalDivider, ProfileWithStickerComment, ProfileWithStickerCommentHasBottomMargin, ProfileWithStickerCommentHasSemiDivider, ProfileWithStickerCommentHasNormalDivider, PureStickerCommentHasBottomMargin, PureStickerCommentHasSemiDivider, PureStickerCommentHasNormalDivider, PureCommentHasNormalDivider, PureCommentHasSemiDivider, PureCommentHasBottomMargin, ProfileWithCommentWithoutBubbleTail, ProfileWithCommentWithoutBubbleTailHasBottomMargin, ProfileWithCommentWithoutBubbleTailHasSemiDivider, ProfileWithCommentWithoutBubbleTailHasNormalDivider, ProfileWithStickerCommentWithoutBubbleTail, ProfileWithStickerCommentWithoutBubbleTailHasBottomMargin, ProfileWithStickerCommentWithoutBubbleTailHasSemiDivider, ProfileWithStickerCommentWithoutBubbleTailHasNormalDivider, PureIntegrationBot
    }
}
