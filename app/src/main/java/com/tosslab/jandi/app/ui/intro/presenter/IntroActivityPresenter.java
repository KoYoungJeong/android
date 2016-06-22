package com.tosslab.jandi.app.ui.intro.presenter;

import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class IntroActivityPresenter {

    private static final long MAX_DELAY_MS = 500l;

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
                    moveNextActivityWithRefresh(startForInvite);
                }, t -> {
                    LogUtil.e(Log.getStackTraceString(t));

                    Observable<Throwable> errorShare = Observable.just(t)
                            .share();

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

                    errorShare.filter(e -> !(e instanceof RetrofitException))
                            .subscribe(it -> {
                                model.trackSignInFailAndFlush(-1);
                                moveNextActivity(startForInvite);
                            });

                });

    }

    private void moveNextActivityWithRefresh(boolean startForInvite) {

        Observable<Boolean> loginObservable = Observable.just(!model.isNeedLogin())
                .share();

        loginObservable.filter(it -> it)
                .observeOn(Schedulers.io())
                .concatMap(it -> {
                    try {
                        refreshAccountInfo();
                        return Observable.just(true);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    moveNextActivity(startForInvite);
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (e.getStatusCode() < 500
                                || e.getStatusCode() != JandiConstants.NetworkError.UNAUTHORIZED) {
                            moveNextActivity(startForInvite);
                        }
                    }
                });

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

    void refreshAccountInfo() throws RetrofitException {
        model.refreshAccountInfo();
    }

    void moveNextActivity(final boolean startForInvite) {

        Observable<Boolean> hasTeamObservable = Observable.just(model.hasSelectedTeam() && !startForInvite)
                .share();

        hasTeamObservable.filter(it -> it)
                .observeOn(Schedulers.io())
                .doOnNext(it -> PushUtil.registPush())
                .doOnNext(it -> {
                    if (!model.hasLeftSideMenu() && NetworkCheckUtil.isConnected()) {
                        // LeftSideMenu 가 없는 경우 대비
                        model.refreshEntityInfo();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    model.trackAutoSignInSuccessAndFlush(true);
                    view.moveToMainActivity();
                }, t -> {});

        hasTeamObservable.filter(it -> !it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    model.trackAutoSignInSuccessAndFlush(false);

                    view.moveTeamSelectActivity();
                });
    }

    public interface View {
        void moveToSignHomeActivity();

        void moveToMainActivity();

        void moveTeamSelectActivity();

        void showMaintenanceDialog();

        void showUpdateDialog();

        void finishOnUiThread();
    }

}
