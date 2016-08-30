package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.presenter;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

public interface MentionListPresenter {

    void initializeMentionInitializeQueue();

    void onInitializeMyPage(boolean isRefreshAction);

    void loadMoreMentions(long offset);

    void onClickMention(MentionMessage mention);

    void clearMentionInitializeQueue();

    void addMentionedMessage(ResMessages.Link link);
}
