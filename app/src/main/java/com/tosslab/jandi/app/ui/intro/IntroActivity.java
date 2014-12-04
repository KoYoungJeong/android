package com.tosslab.jandi.app.ui.intro;

import android.app.Activity;

import com.newrelic.agent.android.NewRelic;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.intro.model.IntroActivityModel;
import com.tosslab.jandi.app.ui.intro.viewmodel.IntroActivityViewModel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 2. 자동 로그인 여부를 체크하여 이동한다.
 */
@Fullscreen
@EActivity(R.layout.activity_intro)
public class IntroActivity extends Activity {

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

        introModel.setCallback(new IntroActivityModel.Callback() {
            @Override
            public void onUpdateDialog() {
                introViewModel.showUpdateDialog();
            }

            @Override
            public void onIntroFinish() {
                introViewModel.checkAutoSignIn();
            }
        });

        introModel.checkNewVersion();

    }

    private void registerNewRelicToken() {
        NewRelic.withApplicationToken(JandiConstantsForFlavors.NEWRELIC_TOKEN_ID)
                .start(this.getApplication());
    }

}
