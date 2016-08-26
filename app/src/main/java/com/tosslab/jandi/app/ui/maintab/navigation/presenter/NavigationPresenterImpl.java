package com.tosslab.jandi.app.ui.maintab.navigation.presenter;

import android.preference.PreferenceManager;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.NavigationBadgeEvent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.model.NavigationDataModel;
import com.tosslab.jandi.app.ui.maintab.navigation.model.NavigationModel;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.ArrayList;
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

    @Inject
    public NavigationPresenterImpl(NavigationModel navigationModel,
                                   NavigationDataModel navigationDataModel,
                                   NavigationPresenter.View navigationView) {
        this.navigationModel = navigationModel;
        this.navigationDataModel = navigationDataModel;
        this.navigationView = navigationView;

        initializeTeamInitializeQueue();
    }

    @Override
    public void initializeTeamInitializeQueue() {
        teamInitializeQueue = PublishSubject.create();
        teamInitializeQueueSubscription =
                teamInitializeQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> onInitializeTeams());
    }

    @Override
    public void onInitializeTeams() {
        if (!(NetworkCheckUtil.isConnected())) {
            return;
        }

        final List<Team> teamList = new ArrayList<>();
        navigationModel.getRefreshAccountInfoObservable()
                .concatMap(o -> navigationModel.getTeamsObservable(teamList))
                .concatMap(navigationModel::getPendingTeamsObservable)
                .concatMap(navigationModel::getSortedTeamListObservable)
                .concatMap(navigationModel::getCheckSelectedTeamObservable)
                .doOnNext(pair -> {
                    List<Team> teams = pair.second;
                    Observable.combineLatest(
                            Observable.from(teams)
                                    .filter(team -> team.getStatus() == Team.Status.PENDING)
                                    .count(),
                            Observable.from(teams)
                                    .filter(team -> team.getStatus() == Team.Status.JOINED)
                                    .map(Team::getUnread)
                                    .reduce((prev, current) -> prev + current)
                                    .doOnNext(unread ->
                                            BadgeUtils.setBadge(JandiApplication.getContext(), unread)),
                            (pendingTeams, unreadCount) -> pendingTeams + unreadCount)
                            .subscribe(total -> {
                                EventBus.getDefault().post(new NavigationBadgeEvent(total));
                            });
                })
                .subscribeOn(Schedulers.io())
                .map(pair -> {
                    List<Team> teams = pair.second;
                    return navigationDataModel.getTeamRows(teams);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(teamRows -> {
                    navigationDataModel.removeAllTeamRows();
                    navigationDataModel.addTeamRows(teamRows);
                    navigationView.notifyDataSetChanged();
                }, Throwable::printStackTrace);
    }

    @Override
    public void reInitializeTeams() {
        teamInitializeQueue.onNext(new Object());
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
    public void onInitializePresetNavigationItems() {
        Observable.defer(() -> {
            List<MultiItemRecyclerAdapter.Row<?>> rows = new ArrayList<>();

            List<MultiItemRecyclerAdapter.Row<?>> navigationRows =
                    navigationDataModel.getNavigationRows(navigationModel.getNavigationMenus());
            rows.addAll(navigationRows);

            return Observable.just(rows);
        })
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
    public void onInitOrientations() {
        boolean portraitOnly = JandiApplication.getContext().getResources().getBoolean(R.bool.portrait_only);
        if (portraitOnly) {
            navigationView.setOrientationViewVisibility(false);
        } else {
            String value = PreferenceManager.getDefaultSharedPreferences(
                    JandiApplication.getContext()).getString(Settings.SETTING_ORIENTATION, "0");
            onSetUpOrientation(value);
        }
    }

    @Override
    public void onSetUpOrientation(String selectedValue) {
        //TODO
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