package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by tonyjs on 15. 8. 3..
 */
public class ApplicationUtil {

    public static void startAppMarketAndFinish(Activity activity) {
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

    public static int getDisplaySize(boolean isHeight) {
        DisplayMetrics metrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        return isHeight ? metrics.heightPixels : metrics.widthPixels;
    }
}
