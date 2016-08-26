package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.presentor;

import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
public interface StarredListPresenter {

    void onInitializeStarredList(StarredType starredType);

    void onLoadMoreAction(StarredType starredType, long offset);

    void unStarMessage(long messageId);

    void onStarredMessageClick(StarredMessage message);

    void onFileMessageDeleted(long fileId);

    void onFileCommentMessageDeleted(long commentId);

    void onMessageDeleted(long messageId);

    void onMessageUnStarred(long messageId);

    void onMessageStarred(long messageId, StarredType starredType);

    enum StarredType {
        All(""),
        File("file");

        private String type;

        StarredType(String type) {
            this.type = type;
        }

        public String getName() {
            return type;
        }
    }

    interface View {
        void showCheckNetworkDialog();

        void showEmptyLayout();

        void notifyDataSetChanged();

        void showMoreProgress();

        void hideMoreProgress();

        void setHasMore(boolean hasMore);

        void showUnStarSuccessToast();

        void moveToMessageList(long teamId, long roomId, int entityType, long linkId);

        void showUnJoinedTopicErrorToast();

        void moveToFileDetail(long fileMessageId, long selectMessageId);

        void moveToPollDetail(long pollId);

        void hideEmptyLayout();
    }

}
