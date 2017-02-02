package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

public interface MentionListPresenter {

    void onInitializeMyPage(boolean isRefreshAction, final boolean doUpdateLastMessage);

    void loadMoreMentions(long offset);

    void onClickMention(MentionMessage mention);

    void addMentionedMessage(ResMessages.Link link);

    void reInitializeIfEmpty();

    void removeMentionedMessage(long linkId);

    void onStarred(long messageId);

    void onUpdateMentionMarker();
}
