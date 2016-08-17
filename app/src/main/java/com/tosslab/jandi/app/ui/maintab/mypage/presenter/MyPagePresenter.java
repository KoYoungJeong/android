package com.tosslab.jandi.app.ui.maintab.mypage.presenter;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

public interface MyPagePresenter {

    void initializeMentionInitializeQueue();

    void onInitializeMyPage(boolean isRefreshAction);

    void onAddPollBadge();

    void onMinusPollBadge();

    void onGetPollBadge();

    void onRetrieveMyInfo();

    void loadMoreMentions(long offset);

    void onClickMention(MentionMessage mention);

    void clearMentionInitializeQueue();

    void addMentionedMessage(ResMessages.Link link);
}
