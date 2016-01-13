package com.tosslab.jandi.app.ui.members.presenter;

import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;

import java.util.List;

/**
 * Created by Tee on 15. x. x..
 */

public interface MembersListPresenter {

    void setView(View view);

    void onEventBusRegister();

    void onEventBusUnregister();

    void onSearch(CharSequence text);

    void onDestroy();

    void inviteMemberToTopic(int entityId);

    void inviteInBackground(List<Integer> invitedUsers, int entityId);

    void initKickableMode(int entityId);

    void onKickUser(int topicId, int userEntityId);

    void onMemberClickForAssignOwner(int topicId, final ChatChooseItem item);

    void onAssignToTopicOwner(int topicId, int memberId);

    interface View {
        void showProgressWheel();

        void dismissProgressWheel();

        void showListMembers(List<ChatChooseItem> topicMembers);

        int getEntityId();

        int getType();

        void moveDirectMessageActivity(int teamId, int userId, boolean isStarred);

        String getSearchText();

        void showInviteSucceed(int memberSize);

        void showInviteFailed(String errMessage);

        void setKickMode(boolean owner);

        void removeUser(int userEntityId);

        void refreshMemberList();

        void showKickSuccessToast();

        void showKickFailToast();

        void showAlreadyTopicOwnerToast();

        void showConfirmAssignTopicOwnerDialog(String userName, String userProfileUrl, int memberId);

        void showAssignTopicOwnerSuccessToast();

        void showAssignTopicOwnerFailToast();

        void setResultAndFinish(int memberId);
    }

}