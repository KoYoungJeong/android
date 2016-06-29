package com.tosslab.jandi.app.ui.maintab.mypage.presenter;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

import java.util.Date;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public interface MyPagePresenter {

    void initializeMentionInitializeQueue();

    void onInitializeMyPage(boolean isRefreshAction);

    void onInitializePollBadge();

    void onRetrieveMyInfo();

    void loadMoreMentions(long offset);

    void onClickMention(MentionMessage mention);

    void onNewMentionComing(long teamId, @Nullable Date latestCreatedAt);

    void clearMentionInitializeQueue();
}
