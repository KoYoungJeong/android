package com.tosslab.jandi.app.ui.settings.viewmodel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

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
    private ProgressWheel progressWheel;

    @AfterViews
    void initViews() {
        progressWheel = new ProgressWheel(context);
        progressWheel.init();
    }

    @UiThread
    public void changeNotificationTagerFailed(String errMessage) {
        ColoredToast.showError(context, errMessage);
    }

    public void showSignoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage(R.string.jandi_sign_out_message)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_setting_sign_out, (dialog, which) -> EventBus.getDefault().post(new SignOutEvent()))
                .create().show();
    }

    @UiThread
    public void showProgressDialog() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }

    }

    @UiThread
    public void dismissProgressDialog() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }

    @UiThread
    public void returnToLoginActivity() {
        IntroActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }
}
