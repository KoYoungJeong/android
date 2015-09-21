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

    void onDestory();

    void inviteMemberToTopic(int entityId);

    void inviteInBackground(List<Integer> invitedUsers, int entityId);

    interface View {
        void showListMembers(List<ChatChooseItem> topicMembers);

        int getEntityId();

        int getType();

        void moveDirectMessageActivity(int teamId, int userId, boolean isStarred);

        String getSearchText();

        void showInviteSucceed(int memberSize);

        void showInviteFailed(String errMessage);
    }

}