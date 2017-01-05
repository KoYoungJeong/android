package com.tosslab.jandi.app.ui.intro.presenter;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.RealmManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignIn;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import javax.inject.Inject;

import io.realm.exceptions.RealmError;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class IntroActivityPresenter {
    private static final long MAX_DELAY_MS = 100;
    private static final int DAYS_30 = 30;

    IntroActivityModel model;
    View view;

    @Inject
    public IntroActivityPresenter(View view, IntroActivityModel model) {
        this.model = model;
        this.view = view;
    }

    public void checkNewVersion(boolean startForInvite) {
        if (!JandiPreference.isPutVersionCodeStamp()) {
            MessageRepository.getRepository().deleteAllLink();
            SendMessageRepository.getRepository().deleteAllMessages();
            JandiPreference.putVersionCodeStamp();
        }


        if (!model.isNetworkConnected()) {
            // 네트워크 연결 상태 아니면 로그인 여부만 확인하고 넘어감
            if (!model.isNeedLogin()) {
                moveNextActivity(startForInvite);
            } else {
                // 디자인 요청사항 처음에 딜레이가 있어달라는..
                view.moveToSignHomeActivity();
            }

            return;
        }

        Observable.fromCallable(() -> model.getConfigInfo())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(config -> {
                    int installedAppVersion = model.getInstalledAppVersion();
                    if (config.maintenance != null && config.maintenance.status) {
                        view.showMaintenanceDialog();
                        return false;
                    } else if (installedAppVersion < config.versions.android) {
                        view.showUpdateDialog();
                        return false;
                    } else {
                        return true;
                    }
                })
                .observeOn(Schedulers.io())
                .filter(it -> it)
                .doOnNext(it -> this.clearLinkRepositoryIfFirstTime())
                .subscribe(it -> {
                    moveNextActivityWithAccountRefresh(startForInvite);
                }, t -> {
                    LogUtil.e(Log.getStackTraceString(t));

                    Observable<Throwable> errorShare = Observable.just(t)
                            .share();

                    // 네트워크 에러인 경우
                    errorShare.ofType(RetrofitException.class)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(e -> {
                                int statusCode = e.getStatusCode();
                                model.trackSignInFailAndFlush(statusCode);
                                if (statusCode == JandiConstants.NetworkError.SERVICE_UNAVAILABLE) {
                                    view.showMaintenanceDialog();
                                } else {
                                    moveNextActivity(startForInvite);
                                }
                            });

                    // 알수 없는 에러인 경우
                    errorShare.filter(e -> !(e instanceof RetrofitException))
                            .subscribe(it -> {
                                model.trackSignInFailAndFlush(-1);
                                moveNextActivityWithAccountRefresh(startForInvite);
                            });

                });
    }

    private void moveNextActivityWithAccountRefresh(boolean startForInvite) {

        Observable<Boolean> loginObservable = Observable.just(!model.isNeedLogin())
                .share();

        // 로그인이 완료된 경우
        loginObservable.filter(it -> it)
                .subscribe(it -> {
                    moveNextActivity(startForInvite);
                });

        // 로그인이 안 된 경우
        loginObservable.filter(it -> !it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.moveToSignHomeActivity();
                });
    }

    private void clearLinkRepositoryIfFirstTime() {
        /*
        발생하는 경우의 수
        1. 앱 설치/업데이트 후 첫 실행
        2. 로그아웃 직후
         */
        if (!JandiPreference.isClearedLink()) {
            model.clearLinkRepository();
            JandiPreference.setClearedLink();
            LogUtil.d("clearLinkRepository()");
        }
    }

    void moveNextActivity(final boolean startForInvite) {

        Observable<Boolean> hasTeamObservable = Observable.just(model.hasSelectedTeam() && !startForInvite)
                .share();

        // 팀 정보가 있는 경우
        hasTeamObservable.filter(it -> it)
                .doOnNext(it -> PushUtil.registPush())
                .observeOn(Schedulers.computation())
                // 서비스 실행상태 확인 하고 넘김
                .map(it -> JandiSocketService.isServiceRunning(JandiApplication.getContext()))
                .doOnNext(it -> {
                    if (NetworkCheckUtil.isConnected()) {

                        long diffTime = System.currentTimeMillis() - JandiPreference.getSocketConnectedLastTime();

                        // 30 일간 갱신 기록이 없으면 start api를 부르기 위한 사전 작업을 함
                        if ((diffTime / (1000 * 60 * 60 * 24)) >= DAYS_30) {
                            InitialInfoRepository.getInstance().clear();
                            model.clearLinkRepository();
                        }

                        if (!model.hasLeftSideMenu()) {
                            // LeftSideMenu 가 없는 경우 대비
                            model.refreshEntityInfo();
                        }

                        model.refreshRankIfNeeds();
                    }
                    if (!it) {
                        view.startSocketService();
                    }
                })
                .doOnNext(it -> {
                    SprinklrSignIn.sendLog(true, true);
                    AnalyticsUtil.flushSprinkler();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    if (model.hasRank()) {
                        view.moveToMainActivity(!it);
                    } else {
                        view.showDialogNoRank();
                    }
                }, t -> {
                    t.printStackTrace();
                    if (t instanceof RealmError
                            || t instanceof RealmException
                            || t instanceof RealmFileException) {
                        Crashlytics.logException(t);
                        RealmManager.deleteReamAndInit();
                        view.restartIntroActivity();
                    } else {
                        view.showDialogNoRank();
                    }
                });

        // 팀 정보가 없거나 초대에 의해 시작한 경우
        hasTeamObservable.filter(it -> !it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    SprinklrSignIn.sendLog(false, true);
                    AnalyticsUtil.flushSprinkler();
                    view.moveTeamSelectActivity();
                });
    }

    public interface View {
        void moveToSignHomeActivity();

        void moveToMainActivity(boolean needDelay);

        void moveTeamSelectActivity();

        void showMaintenanceDialog();

        void showUpdateDialog();

        void startSocketService();

        void showDialogNoRank();

        void restartIntroActivity();
    }

}
