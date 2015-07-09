package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

/**
 * Created by tonyjs on 15. 7. 9..
 */
@EBean
public class AlertUtil {
    public void showCheckNetworkDialog(Activity activity,
                                       DialogInterface.OnClickListener confirmListener) {
        showDialog(activity,
                null, activity.getString(R.string.err_network),
                activity.getString(R.string.jandi_confirm), confirmListener,
                null, null,
                null, null,
                false);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showDialog(Activity activity,
                           String title, String message,
                           String positive, DialogInterface.OnClickListener positiveListener,
                           String neutral, DialogInterface.OnClickListener neutralListener,
                           String negative, DialogInterface.OnClickListener negativeListener,
                           boolean cancelable) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        if (!TextUtils.isEmpty(positive)) {
            builder.setPositiveButton(positive, positiveListener);
        }
        if (!TextUtils.isEmpty(neutral)) {
            builder.setPositiveButton(neutral, neutralListener);
        }
        if (!TextUtils.isEmpty(negative)) {
            builder.setPositiveButton(negative, negativeListener);
        }
        builder.setCancelable(cancelable);

        builder.create().show();
    }

}
