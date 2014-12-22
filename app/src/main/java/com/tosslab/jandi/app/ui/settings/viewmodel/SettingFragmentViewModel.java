package com.tosslab.jandi.app.ui.settings.viewmodel;

import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class SettingFragmentViewModel {

    private final Logger log = Logger.getLogger(SettingFragmentViewModel.class);

    @RootContext
    Context context;

    @Bean
    JandiEntityClient mJandiEntityClient;

    @Background
    public void changeNotificationTarget(String notificationTarget) {
//        try {
//            mJandiEntityClient.setNotificationTarget(notificationTarget);
//            log.debug("notification target has been changed : " + notificationTarget);
//        } catch (JandiNetworkException e) {
//            log.error("change notification target failed");
//            changeNotificationTagerFailed("변환에 실패했습니다");
//        }
    }

    @UiThread
    public void changeNotificationTagerFailed(String errMessage) {
        ColoredToast.showError(context, errMessage);
    }

    /**
     * Push 설정
     */
    @SupposeUiThread
    public void returnToLoginActivity() {

        // Access Token 삭제
        JandiPreference.clearMyToken(context);

        Intent intent = new Intent(context, IntroActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
