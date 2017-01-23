package com.tosslab.jandi.app.ui.maintab.presenter;

import com.tosslab.jandi.app.network.models.ResConfig;

public interface MainTabPresenter {

    void onInitTopicBadge();

    void onInitChatBadge();

    void onInitMyPageBadge(boolean withUnreadMention);

    void onCheckIfNotLatestVersion();

    void onCheckIfNotProfileSetUp();

    void onCheckIfNOtShowInvitePopup();

    interface View {
        void showInvitePopup();

        void setTopicBadge(int count);

        void setChatBadge(int count);

        void setMypageBadge(int count);

        void showUpdateVersionDialog(ResConfig configInfo);

        void moveSetProfileActivity();
    }

}
