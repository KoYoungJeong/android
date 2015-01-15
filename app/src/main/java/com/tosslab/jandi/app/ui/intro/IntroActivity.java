package com.tosslab.jandi.app.ui.intro;

import android.app.Activity;

import com.newrelic.agent.android.NewRelic;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.ui.intro.viewmodel.IntroActivityViewModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 2. 자동 로그인 여부를 체크하여 이동한다.
 */
@Fullscreen
@EActivity(R.layout.activity_intro)
public class IntroActivity extends Activity {

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
        boolean isNewVersion = introModel.checkNewVersion();
        if (!isNewVersion) {
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

    }

    @Background
    void refreshTokenAndGoNextActivity(long initTime) {
        try {
            introModel.refreshAccountInfo();
            introModel.refreshEntityInfo();
            introModel.sleep(initTime, MAX_DELAY_MS);
            introViewModel.moveMainOrTeamSelectActivity();
        } catch (JandiNetworkException e) {
            if (e.httpStatusCode == 401) {
                // 인증 에러시 재로그인 처리
                introModel.clearTokenInfo();
                introModel.clearAccountInfo();

                introModel.sleep(initTime, MAX_DELAY_MS);
                introViewModel.moveToIntroTutorialActivity();
            } else {
                introViewModel.showWarningToast(getString(R.string.err_network));
                finishOnUiThread();
            }
        }
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
