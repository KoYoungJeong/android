package com.tosslab.jandi.app.utils.logger;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.BuildConfig;

/**
 * Created by Steve SeongUg Jung on 15. 5. 19..
 */
public class LogUtil {

    public static final String TAG = "JANDI";
    public static boolean LOG = BuildConfig.DEBUG;

    public static void d(String tag, String message) {
        if (LOG && !TextUtils.isEmpty(message)) {
            Log.d(tag, message);
        }
    }

    public static void d(String message) {
        d(TAG, message);
    }

    public static void e(String tag, String message) {
        if (LOG && !TextUtils.isEmpty(message)) {
            Log.e(tag, message);
        }
    }

    public static void e(String message) {
        e(TAG, message);
    }

    public static void e(String message, Throwable t) {
        if (LOG && !TextUtils.isEmpty(message)) {
            Log.e(TAG, message, t);
        }
    }

    public static void i(String tag, String message) {
        if (LOG && !TextUtils.isEmpty(message)) {
            Log.i(tag, message);
        }
    }

    public static void i(String message) {
        i(TAG, message);
    }

    public static void w(String tag, String message) {
        if (LOG && !TextUtils.isEmpty(message)) {
            Log.w(tag, message);
        }
    }

    public static void w(String message) {
        w(TAG, message);
    }
}
