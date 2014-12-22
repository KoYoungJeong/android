package com.tosslab.jandi.app.ui.maintab.more;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 14. 12. 22..
 */
@EBean
public class MainMoreFragPresenter {

    @RootContext
    Context context;

    public void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage(R.string.jandi_sign_out_message)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_setting_sign_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // MainTabActivity 에서 로그아웃 처리.
                        EventBus.getDefault().post(new SignOutEvent());

                    }
                })
                .create().show();
    }
}
