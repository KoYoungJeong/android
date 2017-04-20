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
import com.tosslab.jandi.app.network.models.ResDeviceSubscribe;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.navigation.adapter.model.NavigationDataModel;
import com.tosslab.jandi.app.ui.maintab.navigation.model.NavigationModel;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.DeviceUtil;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrInvitationAccept;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.intercom.android.sdk.Company;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UserAttributes;
import io.intercom.android.sdk.identity.Registration;
import rx.Completable;
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
                        .observeOn(Schedulers.io())
                        .doOnNext(o -> {
                            if (NetworkCheckUtil.isConnected()) {
                                navigationModel.refreshAccountInfo();
                            }
                        })
                        .concatMap(o -> navigationModel.getTeamsObservable())
                        .doOnNext(teams -> {
                            ResAccountInfo.UserTeam selectedTeamInfo =
                                    AccountRepository.getRepository().getSelectedTeamInfo();
                            if (selectedTeamInfo != null) {
                                Observable.from(teams)
                                        .takeFirst(team -> selectedTeamInfo.getTeamId() == team.getTeamId())
                                        .subscribe(team -> team.setSelected(true), t -> {
                                        });
                            }
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
                        .doOnNext(this::initLauncherBadgeCount)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(navigationDataModel::getTeamRows)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(teamRows -> {
                            navigationDataModel.removeAllTeamRows();
                            navigationDataModel.addTeamRows(teamRows);
                            navigationView.notifyDataSetChanged();
                        }, Throwable::printStackTrace);

    }

    @Override
    public void initBadgeCount() {
        List<Team> teams = navigationDataModel.getTeams();
        initBadgeCount(teams);
        initLauncherBadgeCount(teams);
    }

    private void initBadgeCount(List<Team> teams) {

        if (teams == null || teams.isEmpty()) {
            return;
        }

        Observable.combineLatest(
                Observable.from(teams)
                        .filter(team -> team.getStatus() == Team.Status.PENDING)
                        .count(),
                Observable.from(teams)
                        .filter(team -> team.getStatus() == Team.Status.JOINED)
                        .filter(team -> team.getTeamId() != TeamInfoLoader.getInstance().getTeamId())
                        .map(Team::getUnread)
                        .defaultIfEmpty(0)
                        .reduce((prev, current) -> prev + current),
                Observable.defer(() -> {
                    try {
                        return Observable.just(Intercom.client().getUnreadConversationCount());
                    } catch (Exception e) {
                        return Observable.just(0);
                    }
                }),
                (pendingTeams, unreadCount, intercomCount) -> pendingTeams + unreadCount + intercomCount)
                .subscribe(total -> {
                    EventBus.getDefault().post(new NavigationBadgeEvent(total));
                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                });
    }

    @Override
    public void onInitializeTeams() {
        if (!teamInitializeQueueSubscription.isUnsubscribed()) {
            teamInitializeQueue.onNext(new Object());
        }
    }

    @Override
    public void initLauncherBadgeCount(List<Team> teams) {
        if (teams == null || teams.isEmpty()) {
            return;
        }
        Observable.from(teams)
                .filter(team -> team.getStatus() == Team.Status.JOINED)
                .map(Team::getUnread)
                .defaultIfEmpty(0)
                .reduce((prev, current) -> prev + current)
                .subscribe(totalActivedBadge -> {
                    BadgeUtils.setBadge(JandiApplication.getContext(), totalActivedBadge);
                }, t -> {
                    LogUtil.e(TAG, Log.getStackTraceString(t));
                });
    }

    @Override
    public void onTeamJoinAction(long teamId) {
        if (navigationModel.isCurrentTeam(teamId)) {
            navigationView.closeNavigation();
            return;
        }

        navigationView.showProgressWheel();
        navigationModel.getUpdateEntityInfoObservable(teamId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    navigationView.dismissProgressWheel();
                    navigationView.moveToSelectTeam();
                }, t -> {
                    navigationView.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getStatusCode() == 403) {
                            navigationView.moveTeamList();
                        }
                    }
                });
    }

    @Override
    public void onTeamInviteAcceptAction(final Team team) {
        navigationView.showProgressWheel();

        navigationModel.getInviteDecisionObservable(
                team.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType())
                .concatMap(resTeamDetailInfo -> {
                    resTeamDetailInfo.getInviteTeam().getTeamMembers();
                    SprinklrInvitationAccept.sendLog(resTeamDetailInfo.getInviteTeam().getTeamId(),
                            resTeamDetailInfo.getMember().getId());
                    return navigationModel.getUpdateTeamInfoObservable(team.getTeamId());
                })

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
                        SprinklrInvitationAccept.sendFailLog(((RetrofitException) error).getResponseCode());
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
                }, t -> {
                    navigationView.dismissProgressWheel();

                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getStatusCode() == 403) {
                            navigationView.moveTeamList();
                        }
                    }
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
                    MenuItem itemNotification = menuBuilder.findItem(R.id.nav_setting_notification);

                    int notificationState = getAlarmStatus();

                    if (itemNotification != null) {
                        if (notificationState == 0) {
                            itemNotification.setIcon(
                                    JandiApplication.getContext().getDrawable(R.drawable.side_bar_notifications_on));
                        } else if (notificationState == 1) {
                            itemNotification.setIcon(
                                    JandiApplication.getContext().getDrawable(R.drawable.side_bar_notifications_off));
                        } else if (notificationState == 2) {
                            itemNotification.setIcon(
                                    JandiApplication.getContext().getDrawable(R.drawable.side_bar_notifications_schedule));
                        }
                    }

                    MenuItem itemOrientation = menuBuilder.findItem(R.id.nav_setting_orientation);
                    if (itemOrientation != null) {
                        itemOrientation.setVisible(!(navigationModel.isPhoneMode()));
                    }
                    MenuItem itemCallPreview = menuBuilder.findItem(R.id.nav_setting_call_preview);
                    if (itemCallPreview != null) {
                        itemCallPreview.setVisible(DeviceUtil.isCallableDevice());
                    }

                    MenuItem domainItem = menuBuilder.findItem(R.id.nav_change_domain);
                    if (domainItem != null) {
                        domainItem.setVisible(AccountRepository.getRepository().hasTeamInfo(279));
                    }
                })
                .map(navigationDataModel::getNavigationRows)
                .doOnNext(rows -> {
                    String versionName = SettingsModel.getVersionName();
                    rows.add(navigationDataModel.getVersionRow(versionName));
                })
                .subscribeOn(Schedulers.computation())
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
    public void onMessageRead(boolean fromSelf, long teamId, int readCount) {
        if (fromSelf && readCount > 0) {
            if (!badgeCountingQueueSubscription.isUnsubscribed()) {
                badgeCountingQueue.onNext(Pair.create(teamId, -readCount));
            }
        } else {
            // 다른 플랫폼에서 메세지 관련 이벤트가 발생하면
            // 서버로 Account 정보만 요청해서 refresh 함
            Completable.fromAction(navigationModel::refreshAccountInfo)
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> {
                        onReloadTeams();

                    });
        }
    }

    @Override
    public void onInitIntercom() {

        Completable.fromAction(() -> {
            Registration it = Registration.create();
            ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
            it.withUserId(accountInfo.getUuid());
            Intercom.client().registerIdentifiedUser(it);

            long myId = TeamInfoLoader.getInstance().getMyId();
            User user = TeamInfoLoader.getInstance().getUser(myId);

            Company company = new Company.Builder()
                    .withCompanyId(TeamInfoLoader.getInstance().getTeamId() + "")
                    .build();

            UserAttributes userAttributes = new UserAttributes.Builder()
                    .withName(user.getName())
                    .withEmail(user.getEmail())
                    .withCustomAttribute("create_at", accountInfo.getCreatedAt())
                    .withCustomAttribute("language_override",
                            Locale.getDefault().getDisplayLanguage())
                    .withCompany(company)
                    .build();

            Intercom.client().updateUser(userAttributes);
            Intercom.client().setInAppMessageVisibility(Intercom.Visibility.GONE);

        }).subscribeOn(Schedulers.computation())
                .subscribe(() -> {
                }, t -> {
                });
    }

    @Override
    public void onReloadTeams() {
        navigationModel.getTeamsObservable()
                .flatMap(Observable::from)
                .concatMap(team -> Observable.from(navigationDataModel.getTeams())
                        .takeFirst(it -> it.getTeamId() == team.getTeamId())
                        .doOnNext(it -> it.setUnread(team.getUnread())))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team -> navigationView.notifyDataSetChanged(),
                        Throwable::printStackTrace,
                        this::initBadgeCount);
    }

    @Override
    public void onLaunchHelpPage() {
        navigationView.launchHelpPage(SettingsModel.getSupportUrl());
    }

    @Override
    public void onInitUserProfile() {
        try {
            User me = navigationModel.getMe();
            navigationView.setUserProfile(me);
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void initScheduleCache() {
        if (!Settings.hasAlarmScheduleCache()) {
            Observable.defer(() -> {
                ResDeviceSubscribe resDeviceSubscribe = navigationModel.getDeviceInfo();
                return Observable.just(resDeviceSubscribe);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deviceInfo -> {
                        if (deviceInfo.getDays() != null && deviceInfo.getDays().size() > 0) {
                            Settings.setHasAlarmSchedule(true);
                            Settings.setPreferencePushAlarmSchedule(true);
                            Settings.setPreferencePushAlarmScheduleDays(deviceInfo.getDays());
                            Settings.setPreferencePushAlarmScheduleStartTime(deviceInfo.getStartTime());
                            Settings.setPreferencePushAlarmScheduleEndTime(deviceInfo.getEndTime());
                            Settings.setPreferencePushAlarmScheduleTimeZone(deviceInfo.getTimeZone());
                        }
                    });
        }
    }

    // 0 : 푸쉬 알림 수신 가능 1 : 푸쉬 알림 수신 불가능 2 : 푸쉬 알림 스케쥴 수신 불가능
    private int getAlarmStatus() {
        if (!Settings.isPushOn()) {
            return 1;
        }

        if (!Settings.getPreferencePushAlarmSchedule()) {
            return 0;
        } else {
            Calendar calendar = Calendar.getInstance();
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);

            int weekdayOfJandiNum = 0;

            switch (weekday) {
                case Calendar.MONDAY:
                    weekdayOfJandiNum = 0;
                    break;
                case Calendar.TUESDAY:
                    weekdayOfJandiNum = 1;
                    break;
                case Calendar.WEDNESDAY:
                    weekdayOfJandiNum = 2;
                    break;
                case Calendar.THURSDAY:
                    weekdayOfJandiNum = 3;
                    break;
                case Calendar.FRIDAY:
                    weekdayOfJandiNum = 4;
                    break;
                case Calendar.SATURDAY:
                    weekdayOfJandiNum = 5;
                    break;
                case Calendar.SUNDAY:
                    weekdayOfJandiNum = 6;
                    break;
            }

            List<Integer> scheduledWeekdays = Settings.getPreferencePushAlarmScheduleDays();

            for (int scheduledWeekday : scheduledWeekdays) {
                int startTime = Settings.getPreferencePushAlarmScheduleStartTime();
                int endTime = Settings.getPreferencePushAlarmScheduleEndTime();
                int timezone = Settings.getPreferencePushAlarmScheduleTimeZone();
                int timezoneDistanceFromKorea = timezone - 9;
                DateFormat sdf = new SimpleDateFormat("HHmm");
                int currentTime = Integer.valueOf(sdf.format(calendar.getTime()));
                currentTime += timezoneDistanceFromKorea * 100;

                if (endTime < startTime) {
                    if (weekdayOfJandiNum == scheduledWeekday) {
                        if (startTime < currentTime) {
                            return 0;
                        }
                    } else {
                        int tempScheduledWeekday = scheduledWeekday + 1;
                        if (tempScheduledWeekday == 7) {
                            tempScheduledWeekday = 0;
                        }
                        if (weekdayOfJandiNum == tempScheduledWeekday) {
                            if (currentTime < endTime) {
                                return 0;
                            }
                        }
                    }
                } else {
                    if (weekdayOfJandiNum == scheduledWeekday) {
                        if (startTime < currentTime && currentTime < endTime) {
                            return 0;
                        }
                    }
                }
            }
        }
        return 2;
    }

}