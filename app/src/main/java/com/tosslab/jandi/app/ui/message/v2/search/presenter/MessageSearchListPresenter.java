package com.tosslab.jandi.app.ui.message.v2.search.presenter;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.services.socket.to.SocketAnnouncementEvent;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

public interface MessageSearchListPresenter {

    void setView(View view);

    void onDestory();

    void onRequestNewMessage();

    void checkEnabledUser(int entityId);

    boolean onOptionItemSelected(Fragment messageSearchListFragment, MenuItem item, int teamId, int entityId);

    void onInitRoomInfo();

    void setDefaultInfos(int teamId, int roomId, int entityId, int lastMarker, int entityType);

    void onAccouncementOpen();

    void onAnnouncementClose();

    void onCreatedAnnouncement(boolean isForeground, boolean isRoomInit);

    void onUpdateAnnouncement(boolean isForeground, boolean isRoomInit, SocketAnnouncementEvent.Data data);

    void checkAnnouncementExistsAndCreate(int messageId);

    void onDeleteAnnouncement();

    void onMessageItemClick(Fragment fragment, ResMessages.Link item, int entityId);

    void onMessageItemLongClick(ResMessages.Link item);

    void deleteMessage(int messageType, int messageId);

    void onModifyEntity(String inputName);

    void registStarredMessage(int teamId, int messageId);

    void unregistStarredMessage(int teamId, int messageId);

    void onTeamLeave(int teamId, int memberId);

    void onRequestOldMessage();

    interface View {

        void setDisabledUser();

        void setRoomId(int roomId);

        void setLastReadLinkId(int realLastLinkId);

        void setRoomInit(boolean isRoomInit);

        boolean isForeground();

        void dismissProgressWheel();

        void setAnnouncement(ResAnnouncement announcement, boolean announcementOpened);

        void openAnnouncement(boolean opened);

        void showCreateAlertDialog(DialogInterface.OnClickListener p0);

        void showProgressWheel();

        void updateMarkerNewMessage(ResMessages newMessage, boolean isLastLinkId, boolean firstLoad);

        int getFirstVisibleItemLinkId();

        int getItemCount();

        void dismissLoadingView();

        int getFirstVisibleItemTop();

        void updateMarkerMessage(int linkId, ResMessages oldMessage, boolean noFirstLoad, boolean isFirstMessage, int latestVisibleMessageId, int firstVisibleItemTop);

        void showDummyMessageDialog(long localId);

        void moveFileDetailActivity(Fragment fragment, int messageId, int roomId, int selectedMessageId);

        AnalyticsValue.Screen getScreen(int entityId);

        void showMessageMenuDialog(boolean isDirectMessage, boolean isMyMessage, ResMessages.TextMessage textMessage);

        void showMessageMenuDialog(ResMessages.CommentMessage message);

        void deleteLinkByMessageId(int messageId);

        void modifyEntitySucceed(String topicName);

        void showFailToast(String message);

        void showSuccessToast(String message);

        void modifyStarredInfo(int messageId, boolean starred);

        void showLeavedMemberDialog(int entityId);

        void showOldLoadingProgress();

        void dismissOldLoadingProgress();
    }
}
