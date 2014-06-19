package com.tosslab.toss.app.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.tosslab.toss.app.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class TossLogger {
    private static final String LOG_PREFIX = "okrabbit_";

    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();

    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static boolean LOG_TO_FILE = false;

    private static String packageName;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * WARNING: Don't use this when obfuscating class names with Proguard!
     */
    public static String makeLogTag(Class<?> cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);

            appendLog(tag, message);
        }
    }

    public static void LOGD(final String tag, String message, Throwable cause) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message, cause);

            appendLog(tag, message, cause);
        }
    }

    public static void LOGV(final String tag, String message) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message);

            appendLog(tag, message);
        }
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        // noinspection PointlessBooleanExpression,ConstantConditions
        if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, message, cause);

            appendLog(tag, message, cause);
        }
    }

    public static void LOGI(final String tag, String message) {
        Log.i(tag, message);

        appendLog(tag, message);
    }

    public static void LOGI(final String tag, String message, Throwable cause) {
        Log.i(tag, message, cause);

        appendLog(tag, message, cause);
    }

    public static void LOGW(final String tag, String message) {
        Log.w(tag, message);

        appendLog(tag, message);
    }

    public static void LOGW(final String tag, String message, Throwable cause) {
        Log.w(tag, message, cause);

        appendLog(tag, message, cause);
    }

    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);

        appendLog(tag, message);
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);

        appendLog(tag, message, cause);
    }

    private TossLogger() {
    }

    public static void setEnableLogToFile(Context context) {
        String packageName = context.getPackageName();

        if (packageName == null) {
            Log.e("TossLogger", "Can't write log to file");
            LOG_TO_FILE = false;
        } else {
            TossLogger.packageName = packageName;

            LOG_TO_FILE = true;
        }
    }

    public static void appendLog(String tag, String text) {
        appendLog(tag, text, null);
    }

    public static void appendLog(String tag, String text, Throwable cause) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        if (!LOG_TO_FILE) {
            return;
        }

        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        String current = dateFormat.format(currentDate);

        File myFilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/" + packageName + "/baas.io_log");

        if (!myFilesDir.exists() || !myFilesDir.isDirectory()) {
            myFilesDir.mkdirs();
        }

        File logFile = new File(myFilesDir + "/" + tag + "_" + current + ".txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            String currentDateTimeString = SimpleDateFormat.getDateTimeInstance().format(
                    currentDate);
            buf.append(currentDateTimeString);
            buf.newLine();
            buf.append(text);
            buf.newLine();
            if (cause != null) {
                buf.append(cause.getStackTrace().toString());
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
