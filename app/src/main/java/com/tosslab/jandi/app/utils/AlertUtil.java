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

    private static final int NONE_RES_ID = -1;

    public void showCheckNetworkDialog(Activity activity,
                                       DialogInterface.OnClickListener confirmListener) {
        showDialog(activity,
                NONE_RES_ID, R.string.err_network,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                false);
    }

    public void showConfirmDialog(Activity activity, int messageResId,
                                  DialogInterface.OnClickListener confirmListener,
                                  boolean cancelable) {
        showDialog(activity,
                NONE_RES_ID, messageResId,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                cancelable);
    }

    public void showConfirmDialog(Activity activity, int titleResId, int messageResId,
                                  DialogInterface.OnClickListener confirmListener,
                                  boolean cancelable) {
        showDialog(activity,
                titleResId, messageResId,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                cancelable);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showDialog(Activity activity,
                            int titleResId, int messageResId,
                            int positiveResId, DialogInterface.OnClickListener positiveListener,
                            int neutralResId, DialogInterface.OnClickListener neutralListener,
                            int negativeResId, DialogInterface.OnClickListener negativeListener,
                            boolean cancelable) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (titleResId != NONE_RES_ID) {
            builder.setTitle(activity.getString(titleResId));
        }
        if (messageResId != NONE_RES_ID) {
            builder.setMessage(activity.getString(messageResId));
        }
        if (positiveResId != NONE_RES_ID) {
            builder.setPositiveButton(activity.getString(positiveResId), positiveListener);
        }
        if (neutralResId != NONE_RES_ID) {
            builder.setNeutralButton(activity.getString(neutralResId), neutralListener);
        }
        if (negativeResId != NONE_RES_ID) {
            builder.setNegativeButton(activity.getString(negativeResId), negativeListener);
        }
        builder.setCancelable(cancelable);

        builder.create().show();
    }

}