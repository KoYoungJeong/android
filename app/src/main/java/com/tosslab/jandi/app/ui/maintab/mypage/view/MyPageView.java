package com.tosslab.jandi.app.ui.maintab.mypage.view;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public interface MyPageView {
    void setMe(User me);

    void showProgress();

    void hideProgress();

    void showMoreProgress();

    void showEmptyMentionView();

    void clearMentions();

    void setHasMore(boolean hasMore);

    void addMentions(List<MentionMessage> mentions);

    void hideMoreProgress();

    void moveToFileDetailActivity(long fileId, long messageId);

    void showUnknownEntityToast();

    void moveToMessageListActivity(long teamId, long entityId, int roomTypeId, long roomId, long linkId);

    void notifyDataSetChanged();

    void hideRefreshProgress();

    void clearLoadMoreOffset();

    void hideEmptyMentionView();
}
