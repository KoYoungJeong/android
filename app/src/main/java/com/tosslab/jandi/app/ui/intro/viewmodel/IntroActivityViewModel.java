package com.tosslab.jandi.app.ui.intro.viewmodel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityViewModel {

    @RootContext
    Activity activity;

    @UiThread
    public void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.jandi_update_title)
                .setMessage(R.string.jandi_update_message)
                .setPositiveButton(R.string.jandi_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final String appPackageName = activity.getPackageName();
                                try {
                                    activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                } finally {
                                    activity.finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
                                }
                            }
                        }
                )
                .setCancelable(false)
                .create()
                .show();
    }

    public void moveMainOrTeamSelectActivity() {

        ResAccountInfo.UserTeam mySelectedTeam = JandiAccountDatabaseManager.getInstance(activity).getSelectedTeamInfo();

        if (mySelectedTeam != null) {
            moveToMainActivity();
        } else {
            moveTeamSelectActivity();
        }
    }

    @UiThread
    void moveTeamSelectActivity() {
        AccountHomeActivity_
                .intent(activity)
                .start();
        activity.finish();
    }

    @UiThread
    void moveToMainActivity() {
        // Move MainActivity
        MainTabActivity_.intent(activity)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        activity.finish();
    }

    @UiThread
    public void moveToIntroTutorialActivity() {
        // Move TutorialActivity
        IntroMainActivity_.intent(activity).start();
        activity.finish();
    }

    @UiThread
    public void showWarningToast(String message) {
        ColoredToast.showWarning(activity, message);

    }

    @UiThread
    public void showMaintenanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.jandi_service_maintenance)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> activity.finish())
                .setCancelable(false)
                .create().show();
    }
}
