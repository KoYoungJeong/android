package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public interface BodyViewHolder {

    void initView(View rootView);

    void bindData(ResMessages.Link link, int teamId, int roomId, int entityId);

    void setLastReadViewVisible(int currentLinkId, int lastReadLinkId);

    int getLayoutId();

    void setOnItemClickListener(View.OnClickListener itemClickListener);

    void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener);

    enum Type {
        Message, PureMessage, PureLinkPreviewMessage,
        Sticker, PureSticker,
        File, FileWithoutDivider,
        Image, ImageWithoutDivider,
        FileComment, FileCommentWioutDivider,
        PureComment, PureCommentWioutDivider,
        CollapseComment, CollapseCommentWioutDivider,
        FileStickerComment, FileStickerCommentWioutDivider,
        PureStickerComment, PureStickerCommentWioutDivider,
        CollapseStickerComment, CollapseStickerCommentWioutDivider,
        Dummy, DummyPure,
        Empty,
        Event,
        JandiBot, CollapseJandiBot, CollapseLinkPreviewJandiBot,
        IntegrationBot, CollapseIntegrationBot
    }
}
