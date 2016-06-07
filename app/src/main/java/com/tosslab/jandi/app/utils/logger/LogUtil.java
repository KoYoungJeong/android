package com.tosslab.jandi.app.utils.logger;

import android.util.Log;

import com.tosslab.jandi.app.BuildConfig;

/**
 * Created by Steve SeongUg Jung on 15. 5. 19..
 */
public class LogUtil {

    public static final String TAG = "JANDI";
    public static boolean LOG = BuildConfig.DEBUG;
    public static void d(String tag, String message) {
        if (LOG) {
            Log.d(tag, message);
        }
    }

    public static void d(String message) {
        d(TAG, message);
    }

    public static void e(String tag, String message) {
        if (LOG) {
            Log.e(tag, message);
        }
    }

    public static void e(String message) {
        e(TAG, message);
    }

    public static void e(String message, Throwable t) {
        if (LOG) {
            Log.e(TAG, message, t);
        }
    }

    public static void i(String tag, String message) {
        if (LOG) {
            Log.i(tag, message);
        }
    }

    public static void i(String message) {
        i(TAG, message);
    }

    public static void w(String tag, String message) {
        if (LOG) {
            Log.w(tag, message);
        }
    }

    public static void w(String message) {
        w(TAG, message);
    }
}
