package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.view;

import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public interface MentionListView {
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

    void addNewMention(MentionMessage mentionMessages);

    void moveToPollDetailActivity(long pollId);

}