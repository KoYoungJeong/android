package com.tosslab.jandi.lib.sprinkler;

import android.util.Log;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public class Logger {

    private static final String DEFAULT_TAG = "Sprinkler";

    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (!Sprinkler.IS_DEBUG_MODE) {
            return;
        }
        Log.d(tag, msg);
    }

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (!Sprinkler.IS_DEBUG_MODE) {
            return;
        }
        Log.e(tag, msg);
    }

    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (!Sprinkler.IS_DEBUG_MODE) {
            return;
        }
        Log.i(tag, msg);
    }

    public static void w(String msg) {
        w(DEFAULT_TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (!Sprinkler.IS_DEBUG_MODE) {
            return;
        }
        Log.w(tag, msg);
    }

    public static void print(Throwable throwable) {
        if (!Sprinkler.IS_DEBUG_MODE) {
            return;
        }
        throwable.printStackTrace();
    }

    public static String makeTag(Class<?> clazz) {
        return "Sprinkler" + "." + clazz.getSimpleName();
    }

}
