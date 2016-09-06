package com.tosslab.jandi.app.ui.maintab.presenter;

import com.tosslab.jandi.app.network.models.ResConfig;

import rx.functions.Action0;

/**
 * Created by tonyjs on 2016. 8. 23..
 */
public interface MainTabPresenter {

    void onInitTopicBadge();

    void onInitChatBadge();

    void onInitMyPageBadge();

    void onCheckIfNotLatestVersion(Action0 completeAction);

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
