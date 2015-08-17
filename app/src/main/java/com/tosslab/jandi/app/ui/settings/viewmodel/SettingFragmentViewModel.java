package com.tosslab.jandi.app.ui.settings.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
@EBean
public class SettingFragmentViewModel {

    @Bean
    EntityClientManager mEntityClientManager;
    private ProgressWheel progressWheel;

    public void initProgress(Activity activity) {
        progressWheel = new ProgressWheel(activity);
    }

    @UiThread
    public void changeNotificationTagerFailed(Context context, String errMessage) {
        ColoredToast.showError(context, errMessage);
    }

    public void showSignoutDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
    public void returnToLoginActivity(Context context) {
        IntroActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showCheckNetworkDialog(Activity activity) {
        AlertUtil.showCheckNetworkDialog(activity, null);
    }
}
