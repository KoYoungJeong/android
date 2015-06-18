package com.tosslab.jandi.app.ui.intro;

import android.support.v7.app.AppCompatActivity;

import com.newrelic.agent.android.NewRelic;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.ui.intro.viewmodel.IntroActivityViewModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 2. 자동 로그인 여부를 체크하여 이동한다.
 */
@Fullscreen
@EActivity(R.layout.activity_intro)
public class IntroActivity extends AppCompatActivity {

    private static final long MAX_DELAY_MS = 1500l;

    @Bean
    IntroActivityModel introModel;

    @Bean
    IntroActivityViewModel introViewModel;

    @AfterInject
    void init() {
        registerNewRelicToken();
    }

    @AfterViews
    void startOn() {

        checkNewVersion();

    }

    @Background
    void checkNewVersion() {
        long initTime = System.currentTimeMillis();
        try {
            ResConfig config = introModel.getConfigInfo();

            int installedAppVersion = introModel.getInstalledAppVersion(IntroActivity.this);
            if (config.maintenance != null && config.maintenance.status) {
                introViewModel.showMaintenanceDialog();
            } else if (installedAppVersion < config.versions.android) {
                introModel.sleep(initTime, MAX_DELAY_MS);
                introViewModel.showUpdateDialog();
            } else {
                if (introModel.hasOldToken()) {
                    introModel.removeOldToken();
                }

                if (!introModel.isNeedLogin()) {
                    refreshTokenAndGoNextActivity(initTime);
                } else {
                    introModel.sleep(initTime, MAX_DELAY_MS);
                    introViewModel.moveToIntroTutorialActivity();
                }
            }

        } catch (JandiNetworkException e) {
            introModel.sleep(initTime, MAX_DELAY_MS);
            introViewModel.showMaintenanceDialog();
        } catch (Exception e) {
            introViewModel.showMaintenanceDialog();
        }

    }

    @Background
    void refreshTokenAndGoNextActivity(long initTime) {

        // like fork & join...but need to refactor

        Observable.combineLatest(Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                new Thread(() -> {
                    try {
                        introModel.refreshAccountInfo();
                        subscriber.onNext(200);
                    } catch (JandiNetworkException e) {
                        subscriber.onNext(e.httpStatusCode);
                    } catch (Exception e) {
                        subscriber.onNext(500);
                    }

                    subscriber.onCompleted();
                }).start();
            }
        }), Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                new Thread(() -> {
                    subscriber.onNext(introModel.refreshEntityInfo() ? 1 : -1);
                    subscriber.onCompleted();
                }).start();

            }
        }), (o, o2) -> o).subscribe(o -> {
            if (o == 200) {
                introModel.sleep(initTime, MAX_DELAY_MS);
                if (introModel.hasSelectedTeam()) {
                    //ParseUpdateUtil.updateParseWithoutSelectedTeam(IntroActivity.this.getApplicationContext());
                    introViewModel.moveToMainActivity();
                } else {
                    introViewModel.moveTeamSelectActivity();
                }
                introModel.updateParseForAllTeam();
            } else if (o == 401) {
                introModel.clearTokenInfo();
                introModel.clearAccountInfo();

                introModel.sleep(initTime, MAX_DELAY_MS);
                introViewModel.moveToIntroTutorialActivity();
            } else {
                introViewModel.showWarningToast(getString(R.string.err_network));
                finishOnUiThread();
            }
        });

    }

    @UiThread
    void finishOnUiThread() {
        finish();
    }

    private void registerNewRelicToken() {
        NewRelic.withApplicationToken(JandiConstantsForFlavors.NEWRELIC_TOKEN_ID)
                .start(this.getApplication());
    }

}
