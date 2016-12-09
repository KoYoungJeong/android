package com.tosslab.jandi.app.ui.members.presenter;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import java.util.List;

/**
 * Created by Tee on 15. x. x..
 */

public interface MembersListPresenter {

    void onInit();

    void onSearch(CharSequence text);

    void onDestroy();

    void inviteMemberToTopic(long entityId);

    void inviteInBackground(List<Long> invitedUsers, long entityId);

    void initKickableMode(long entityId);

    void onKickMemberClick(long topicId, final ChatChooseItem item);

    void onKickUser(long topicId, long userEntityId);

    void onMemberClickForAssignOwner(long topicId, final ChatChooseItem item);

    void onAssignToTopicOwner(long topicId, long memberId);

    interface View {
        void showProgressWheel();

        void dismissProgressWheel();

        void showListMembers(List<ChatChooseItem> topicMembers);

        long getEntityId();

        int getType();

        void moveDirectMessageActivity(long teamId, long userId);

        void showInviteSucceed(int memberSize);

        void showInviteFailed(String errMessage);

        void setKickMode(boolean owner);

        void removeUser(long userEntityId);

        void refreshMemberList();

        void showDialogKick(String userName, String userProfileUrl, long memberId);

        void showKickSuccessToast();

        void showKickFailToast();

        void showAlreadyTopicOwnerToast();

        void showNeedToAssignTopicOwnerDialog();

        void showConfirmAssignTopicOwnerDialog(String userName, String userProfileUrl, long memberId);

        void showAssignTopicOwnerSuccessToast();

        void showAssignTopicOwnerFailToast();

        void setResultAndFinish(long memberId);

        void showDialogGuestKick(long memberId);

        void moveToProfile(long userId);

        void inviteMember(long entityId);
    }

}