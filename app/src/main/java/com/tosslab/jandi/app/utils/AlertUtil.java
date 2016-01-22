package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 15. 7. 9..
 */
public class AlertUtil {

    private static final int NONE_RES_ID = -1;

    public static void showCheckNetworkDialog(Activity activity,
                                              DialogInterface.OnClickListener confirmListener) {
        showDialog(activity,
                NONE_RES_ID, R.string.err_network,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                false);
    }

    public static void showConfirmDialog(Activity activity, int messageResId,
                                         DialogInterface.OnClickListener confirmListener,
                                         boolean cancelable) {
        showDialog(activity,
                NONE_RES_ID, messageResId,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                cancelable);
    }

    public static void showConfirmDialog(Activity activity, String message,
                                         DialogInterface.OnClickListener confirmListener,
                                         boolean cancelable) {
        showDialog(activity,
                null, message,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                cancelable);
    }

    public static void showConfirmDialog(Activity activity, int titleResId, int messageResId,
                                         DialogInterface.OnClickListener confirmListener,
                                         boolean cancelable) {
        showDialog(activity,
                titleResId, messageResId,
                R.string.jandi_confirm, confirmListener,
                NONE_RES_ID, null,
                NONE_RES_ID, null,
                cancelable);
    }

    public static void showDialog(Activity activity,
                                  String title, String message,
                                  int positiveResId, DialogInterface.OnClickListener positiveListener,
                                  int neutralResId, DialogInterface.OnClickListener neutralListener,
                                  int negativeResId, DialogInterface.OnClickListener negativeListener,
                                  boolean cancelable) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
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

    public static void showDialog(Activity activity,
                                  int titleResId, int messageResId,
                                  int positiveResId, DialogInterface.OnClickListener positiveListener,
                                  int neutralResId, DialogInterface.OnClickListener neutralListener,
                                  int negativeResId, DialogInterface.OnClickListener negativeListener,
                                  boolean cancelable) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
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

    public static void showChooseUpdateWebsiteDialog(Activity activity, String appPackageName, int versionCode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_280);
        String[] storeNames = activity.getResources().getStringArray(R.array.jandi_markets);
        builder.setTitle(R.string.jandi_choose_app_store)
                .setItems(storeNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder urlBuilder = new StringBuilder();
                        switch (which) {
                            case 0:
                                // 구글 스토어
                                urlBuilder.append("http://play.google.com/store/apps/details?id=")
                                        .append(appPackageName);
                                break;
                            case 1:
                                // 바이두
                                urlBuilder.append("http://shouji.baidu.com/soft/item?docid=8102225");
                                break;
                            case 2:
                                // 91 apk
                                urlBuilder.append("http://apk.91.com/Soft/Android/")
                                        .append(appPackageName)
                                        .append("-")
                                        .append(versionCode)
                                        .append(".html");
                                break;
                            case 3:
                                // Hi apk
                                urlBuilder.append("http://apk.hiapk.com/appinfo/")
                                        .append(appPackageName);
                                break;
                        }

                        if (urlBuilder.length() > 0) {
                            try {
                                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(urlBuilder.toString())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        activity.finish();
                    }
                })
                .create()
                .show();
    }
}
