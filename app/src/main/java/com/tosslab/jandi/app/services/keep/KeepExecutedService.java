package com.tosslab.jandi.app.services.keep;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.f2prateek.dart.HensonNavigable;
import com.tosslab.jandi.app.Henson;

import java.util.List;

@HensonNavigable
public class KeepExecutedService extends Service {

    // Application 을 계속 유지해서 앱의 로딩을 빠르게 하기 위함
    public static void start(Context context) {
        if (isServiceRunning(context)) {
            return;
        }

        context.startService(Henson.with(context)
                .gotoKeepExecutedService()
                .build());
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            String packageName = runningService.service.getPackageName();
            String className = runningService.service.getClassName();
            if (TextUtils.equals(packageName, context.getPackageName())
                    && TextUtils.equals(className, KeepExecutedService.class.getName())) {
                return true;
            }
        }
        return false;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "KeepExecutedService";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        return START_NOT_STICKY;
    }
}
