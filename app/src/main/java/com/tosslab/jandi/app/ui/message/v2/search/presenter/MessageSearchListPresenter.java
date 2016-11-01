package com.tosslab.jandi.app.ui.message.v2.search.presenter;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

public interface MessageSearchListPresenter {

    void setView(View view);

    void onDestory();

    void onRequestNewMessage();

    void checkEnabledUser(long entityId);

    boolean onOptionItemSelected(Fragment messageSearchListFragment, MenuItem item, long teamId, long entityId);

    void onInitRoomInfo();

    void setDefaultInfos(long teamId, long roomId, long entityId, long lastMarker, int entityType);

    void onAccouncementOpen();

    void onAnnouncementClose();

    void onCreatedAnnouncement(boolean isRoomInit);

    void onUpdateAnnouncement(boolean isForeground, boolean isRoomInit, boolean opened);

    void checkAnnouncementExistsAndCreate(long messageId);

    void onDeleteAnnouncement();

    void onMessageItemClick(Fragment fragment, ResMessages.Link item, long entityId);

    void onMessageItemLongClick(ResMessages.Link item);

    void deleteMessage(int messageType, long messageId);

    void registStarredMessage(long teamId, long messageId);

    void unregistStarredMessage(long teamId, long messageId);

    void onTeamLeave(long teamId, long memberId);

    void onRequestOldMessage();

    interface View {

        void setDisabledUser();

        void setRoomId(long roomId);

        void setLastReadLinkId(long realLastLinkId);

        void setRoomInit(boolean isRoomInit);

        boolean isForeground();

        void dismissProgressWheel();

        void setAnnouncement(Announcement announcement);

        void openAnnouncement(boolean opened);

        void showCreateAlertDialog(DialogInterface.OnClickListener p0);

        void showProgressWheel();

        void updateMarkerNewMessage(ResMessages newMessage, boolean isLastLinkId, boolean firstLoad);

        long getFirstVisibleItemLinkId();

        int getItemCount();

        void dismissLoadingView();

        int getFirstVisibleItemTop();

        void updateMarkerMessage(long linkId, ResMessages oldMessage, boolean noFirstLoad, boolean isFirstMessage, long latestVisibleMessageId, int firstVisibleItemTop);

        void showDummyMessageDialog(long localId);

        void moveFileDetailActivity(Fragment fragment, long messageId, long roomId, long selectedMessageId);

        AnalyticsValue.Screen getScreen(long entityId);

        void showMessageMenuDialog(boolean isDirectMessage, boolean isMyMessage, ResMessages.TextMessage textMessage);

        void showMessageMenuDialog(ResMessages.CommentMessage message);

        void deleteLinkByMessageId(long messageId);

        void modifyEntitySucceed(String topicName);

        void showFailToast(String message);

        void showSuccessToast(String message);

        void modifyStarredInfo(long messageId, boolean starred);

        void showLeavedMemberDialog(long entityId);

        void showOldLoadingProgress();

        void dismissOldLoadingProgress();

        void dismissUserStatusLayout();

        void movePollDetailActivity(long pollId);
    }
}
