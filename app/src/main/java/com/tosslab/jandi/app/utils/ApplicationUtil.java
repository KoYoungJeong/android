package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

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

    public static void startWebBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getAvailableUrl(url)));
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            InternalWebActivity_.intent(context)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .url(url)
                    .start();
        }
    }

    public static String getAvailableUrl(String url) {

        int protocolIndex = url.indexOf("://");
        if (protocolIndex <= 0) {
            return "http://" + url;
        } else {
            String protocol = url.substring(0, protocolIndex);
            String uri = url.substring(protocolIndex);

            StringBuilder builder = new StringBuilder();
            builder.append(protocol.toLowerCase())
                    .append(uri);

            return builder.toString();
        }
    }

    public static int getDisplaySize(boolean isHeight) {
        DisplayMetrics metrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        return isHeight ? metrics.heightPixels : metrics.widthPixels;
    }
}
