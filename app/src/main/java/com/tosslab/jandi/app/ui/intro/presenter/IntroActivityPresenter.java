package com.tosslab.jandi.app.ui.intro.presenter;

import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class IntroActivityPresenter {

    private static final long MAX_DELAY_MS = 100;
    private static final int DAYS_30 = 30;

    IntroActivityModel model;
    View view;
    private Subscription timerSubs;

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

        Observable.create((Subscriber<? super ResConfig> subscriber) -> {
            try {
                subscriber.onNext(model.getConfigInfo());
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }

            subscriber.onCompleted();

        })
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
                .doOnNext(it ->
                        timerSubs = Observable.timer(2000, TimeUnit.MILLISECONDS)
                                .subscribe(a -> {
                                    view.moveToMainActivity();
                                }))
                .doOnNext(it -> PushUtil.registPush())
                .observeOn(Schedulers.io())
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
                    }
                    view.startSocketService();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    model.trackAutoSignInSuccessAndFlush(true);
                }, t -> {
                });

        // 팀 정보가 없거나 초대에 의해 시작한 경우
        hasTeamObservable.filter(it -> !it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    model.trackAutoSignInSuccessAndFlush(false);

                    view.moveTeamSelectActivity();
                });
    }

    public void cancelAll() {
        if (timerSubs != null && !(timerSubs.isUnsubscribed())) {
            timerSubs.unsubscribe();
        }
    }

    public interface View {
        void moveToSignHomeActivity();

        void moveToMainActivity();

        void moveTeamSelectActivity();

        void showMaintenanceDialog();

        void showUpdateDialog();

        void startSocketService();
    }

}
