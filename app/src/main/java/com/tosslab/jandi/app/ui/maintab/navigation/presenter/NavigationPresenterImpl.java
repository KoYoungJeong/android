package com.tosslab.jandi.app.ui.maintab.navigation.presenter;

import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.NavigationBadgeEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.model.NavigationDataModel;
import com.tosslab.jandi.app.ui.maintab.navigation.model.NavigationModel;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public class NavigationPresenterImpl implements NavigationPresenter {

    private final NavigationModel navigationModel;
    private final NavigationDataModel navigationDataModel;
    private final NavigationPresenter.View navigationView;

    private PublishSubject<Object> teamInitializeQueue;
    private Subscription teamInitializeQueueSubscription;

    private PublishSubject<Pair<Long, Integer>> badgeCountingQueue;
    private Subscription badgeCountingQueueSubscription;

    @Inject
    public NavigationPresenterImpl(NavigationModel navigationModel,
                                   NavigationDataModel navigationDataModel,
                                   NavigationPresenter.View navigationView) {
        this.navigationModel = navigationModel;
        this.navigationDataModel = navigationDataModel;
        this.navigationView = navigationView;

        initializeTeamInitializeQueue();
        initializeBadgeCountingQueue();
    }

    @Override
    public void initializeTeamInitializeQueue() {
        teamInitializeQueue = PublishSubject.create();
        teamInitializeQueueSubscription =
                teamInitializeQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .doOnNext(o -> {
                            if (NetworkCheckUtil.isConnected()) {
                                navigationModel.refreshAccountInfo();
                            }
                        })
                        .concatMap(o -> navigationModel.getTeamsObservable())
                        .doOnNext(teams -> {
                            ResAccountInfo.UserTeam selectedTeamInfo =
                                    AccountRepository.getRepository().getSelectedTeamInfo();
                            Observable.from(teams)
                                    .filter(team -> selectedTeamInfo.getTeamId() == team.getTeamId())
                                    .subscribe(team -> team.setSelected(true));
                        })
                        .map(teams -> {
                            if (NetworkCheckUtil.isConnected()) {
                                teams.addAll(navigationModel.getPendingTeams());
                            }
                            return teams;
                        })
                        .doOnNext(teams -> Collections.sort(teams,
                                (team, team2) -> team.getStatus() == Team.Status.PENDING ? -1 : 1))
                        .doOnNext(this::initBadgeCount)
                        .map(navigationDataModel::getTeamRows)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(teamRows -> {
                            navigationDataModel.removeAllTeamRows();
                            navigationDataModel.addTeamRows(teamRows);
                            navigationView.notifyDataSetChanged();
                        }, Throwable::printStackTrace);

    }

    private void initBadgeCount(List<Team> teams) {
        Observable.combineLatest(
                Observable.from(teams)
                        .filter(team -> team.getStatus() == Team.Status.PENDING)
                        .count(),
                Observable.from(teams)
                        .filter(team -> team.getStatus() == Team.Status.JOINED)
                        .map(Team::getUnread)
                        .reduce((prev, current) -> prev + current),
                (pendingTeams, unreadCount) -> {
                    BadgeUtils.setBadge(JandiApplication.getContext(), unreadCount);
                    return pendingTeams + unreadCount;
                })
                .subscribe(total -> {
                    LogUtil.d("tony", "total - " + total);
                    EventBus.getDefault().post(new NavigationBadgeEvent(total));
                }, t -> {
                    LogUtil.e("tony", Log.getStackTraceString(t));
                });
    }

    @Override
    public void onInitializeTeams() {
        if (!teamInitializeQueueSubscription.isUnsubscribed()) {
            teamInitializeQueue.onNext(new Object());
        }
    }

    @Override
    public void onTeamJoinAction(long teamId) {
        if (navigationModel.isCurrentTeam(teamId)) {
            return;
        }

        navigationView.showProgressWheel();
        navigationModel.getUpdateEntityInfoObservable(teamId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    navigationView.dismissProgressWheel();
                    navigationView.moveToSelectTeam();
                }, error -> {
                    navigationView.dismissProgressWheel();
                });
    }

    @Override
    public void onTeamInviteAcceptAction(final Team team) {
        navigationView.showProgressWheel();

        navigationModel.getInviteDecisionObservable(
                team.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType())
                .concatMap(resTeamDetailInfo ->
                        navigationModel.getUpdateTeamInfoObservable(team.getTeamId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    onTeamCreated();
                }, error -> {
                    LogUtil.e(TAG, Log.getStackTraceString(error));
                    navigationView.dismissProgressWheel();

                    if (error instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) error;
                        String errorMessage =
                                navigationModel.getTeamInviteErrorMessage(e, team.getName());
                        navigationView.showTeamInviteAcceptFailDialog(errorMessage, team);
                    }
                });
    }

    @Override
    public void onTeamInviteIgnoreAction(final Team team) {
        navigationView.showProgressWheel();

        navigationModel.getInviteDecisionObservable(
                team.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.DECLINE.getType())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resTeamDetailInfo -> {
                    navigationView.dismissProgressWheel();
                    navigationDataModel.removePendingTeam(team);
                    navigationView.notifyDataSetChanged();
                }, error -> {
                    LogUtil.e(TAG, Log.getStackTraceString(error));
                    navigationView.dismissProgressWheel();

                    if (error instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) error;
                        String errorMessage =
                                navigationModel.getTeamInviteErrorMessage(e, team.getName());
                        navigationView.showTeamInviteIgnoreFailToast(errorMessage);
                    }
                });
    }

    @Override
    public void onTeamCreated() {
        navigationView.showProgressWheel();

        navigationModel.getSelectedTeamObservable()
                .concatMap(userTeam ->
                        navigationModel.getUpdateEntityInfoObservable(userTeam.getTeamId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    navigationView.dismissProgressWheel();
                    navigationView.moveToSelectTeam();
                }, error -> {
                    navigationView.dismissProgressWheel();
                });
    }

    @Override
    public void clearTeamInitializeQueue() {
        if (teamInitializeQueueSubscription != null && !teamInitializeQueueSubscription.isUnsubscribed()) {
            teamInitializeQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void clearBadgeCountingQueue() {
        if (badgeCountingQueueSubscription != null && !badgeCountingQueueSubscription.isUnsubscribed()) {
            badgeCountingQueueSubscription.unsubscribe();
        }
    }

    @Override
    public void onInitializePresetNavigationItems() {
        Observable.just(navigationModel.getNavigationMenus())
                .doOnNext(menuBuilder -> {
                    MenuItem item = menuBuilder.findItem(R.id.nav_setting_orientation);
                    if (item != null && navigationModel.isPhoneMode()) {
                        item.setVisible(false);
                    }
                })
                .map(navigationDataModel::getNavigationRows)
                .subscribeOn(Schedulers.immediate())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> {
                    navigationDataModel.addRows(rows);
                    navigationView.notifyDataSetChanged();
                });
    }

    @Override
    public void onSignOutAction() {
        navigationView.showProgressWheel();

        navigationModel.getSignOutObservable()
                .onErrorReturn(throwable -> new ResCommon())
                .doOnNext(resCommon1 -> {
                    SignOutUtil.removeSignData();
                    BadgeUtils.clearBadge(JandiApplication.getContext());
                    JandiSocketService.stopService(JandiApplication.getContext());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    String toastMessage = JandiApplication.getContext().getString(R.string.jandi_message_logout);
                    navigationView.showSuccessToast(toastMessage);
                    navigationView.dismissProgressWheel();
                    navigationView.moveLoginActivity();
                }, t -> {
                    navigationView.dismissProgressWheel();
                });
    }

    @Override
    public void onInitJandiVersion() {
        String version = SettingsModel.getVersionName();
        navigationView.setVersion(version);
    }

    @Override
    public void initializeBadgeCountingQueue() {
        badgeCountingQueue = PublishSubject.create();
        badgeCountingQueueSubscription =
                badgeCountingQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .map(pair ->
                                Pair.create(navigationDataModel.getTeamById(pair.first), pair.second))
                        .filter(pair -> pair.first != null && pair.first.getTeamId() > 0)
                        .doOnNext(pair -> {
                            Team team = pair.first;
                            int unread = team.getUnread() + pair.second;
                            if (unread < 0) {
                                unread = 0;
                            }
                            team.setUnread(unread);
                            initBadgeCount(navigationDataModel.getTeams());
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(team -> navigationView.notifyDataSetChanged());

    }

    @Override
    public void onMessageDeleted(long teamId) {
        if (!badgeCountingQueueSubscription.isUnsubscribed()) {
            badgeCountingQueue.onNext(Pair.create(teamId, -1));
        }
    }

    @Override
    public void onMessageCreated(long teamId) {
        if (!badgeCountingQueueSubscription.isUnsubscribed()) {
            badgeCountingQueue.onNext(Pair.create(teamId, 1));
        }
    }

    @Override
    public void onMessageRead(boolean fromSelf, long teamId, int readCount) {
        if (fromSelf && readCount > 0) {
            if (!badgeCountingQueueSubscription.isUnsubscribed()) {
                badgeCountingQueue.onNext(Pair.create(teamId, -readCount));
            }
        } else {
            // 다른 플랫폼에서 읽으면 노답인데...
            onInitializeTeams();
        }
    }

    @Override
    public void onLaunchHelpPage() {
        navigationView.launchHelpPage(SettingsModel.getSupportUrl());
    }

    @Override
    public void onInitUserProfile() {
        navigationView.setUserProfile(navigationModel.getMe());
    }


}