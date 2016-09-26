package com.tosslab.jandi.app.ui.maintab.presenter;

import android.util.Log;

import com.tosslab.jandi.app.ui.maintab.model.MainTabModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 2016. 8. 23..
 */
public class MainTabPresenterImpl implements MainTabPresenter {

    private final MainTabModel mainTabModel;
    private final MainTabPresenter.View mainTabView;

    @Inject
    public MainTabPresenterImpl(MainTabModel mainTabModel, View mainTabView) {
        this.mainTabModel = mainTabModel;
        this.mainTabView = mainTabView;
    }

    @Override
    public void onInitTopicBadge() {
        int topicBadgeCount = mainTabModel.getTopicBadgeCount();
        mainTabView.setTopicBadge(topicBadgeCount);
    }

    @Override
    public void onInitChatBadge() {
        int chatBadgeCount = mainTabModel.getChatBadgeCount();
        mainTabView.setChatBadge(chatBadgeCount);
    }

    @Override
    public void onInitMyPageBadge() {
        int myPageBadgeCount = mainTabModel.getMyPageBadgeCount();
        mainTabView.setMypageBadge(myPageBadgeCount);
    }

    @Override
    public void onCheckIfNotLatestVersion(Action0 completeAction) {
        if (!NetworkCheckUtil.isConnected()) {
            completeAction.call();
            return;
        }

        mainTabModel.getConfigInfoObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(configInfo -> {
                    int currentAppVersionCode = mainTabModel.getCurrentAppVersionCode();
                    if (configInfo != null && configInfo.latestVersions != null &&
                            (currentAppVersionCode < configInfo.latestVersions.android)) {
                        final long oneDayMillis = 1000 * 60 * 60 * 24;
                        long timeFromLastPopup =
                                System.currentTimeMillis() - JandiPreference.getVersionPopupLastTime();
                        if (timeFromLastPopup > oneDayMillis) {
                            mainTabView.showUpdateVersionDialog(configInfo);
                            return;
                        }
                    }

                    completeAction.call();
                }, t -> {
                    LogUtil.e(Log.getStackTraceString(t));
                    completeAction.call();
                });
    }

    @Override
    public void refreshInitialInfo() {
        mainTabModel.getRefreshEntityInfoObservable()
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    @Override
    public void onCheckIfNotProfileSetUp() {
        mainTabModel.getMeObservable()
                .filter(me -> !me.isProfileUpdated())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    mainTabView.moveSetProfileActivity();
                }, t -> {
                });
    }

    @Override
    public void onCheckIfNOtShowInvitePopup() {
        if (mainTabModel.needInvitePopup()) {
            mainTabView.showInvitePopup();
        }
    }
}
