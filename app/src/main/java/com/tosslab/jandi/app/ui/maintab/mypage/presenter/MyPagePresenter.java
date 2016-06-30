package com.tosslab.jandi.app.ui.maintab.mypage.presenter;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

import java.util.Date;

public interface MyPagePresenter {

    void initializeMentionInitializeQueue();

    void onInitializeMyPage(boolean isRefreshAction);

    void onInitializePollBadge();

    void onRetrieveMyInfo();

    void loadMoreMentions(long offset);

    void onClickMention(MentionMessage mention);

    void onNewMentionComing(long teamId, @Nullable Date latestCreatedAt);

    void clearMentionInitializeQueue();

    void addMentionedMessage(ResMessages.Link link);
}
