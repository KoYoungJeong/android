package com.tosslab.jandi.app.dummy;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.List;
import java.util.Random;

/**
 * Created by tonyjs on 16. 4. 14..
 */
public class JandiProcessStartService extends IntentService {

    public static final String TAG = JandiProcessStartService.class.getSimpleName();

    public JandiProcessStartService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (System.currentTimeMillis() - JandiPreference.getLastExecutedTime() < 1000 * 60 * 60 * 24) {
            return;
        }

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        if (runningTasks != null
                && runningTasks.size() > 0) {
            String currentTopAppPackageName = runningTasks.get(0).topActivity.getPackageName();
            if (TextUtils.equals(currentTopAppPackageName, getPackageName())) {
                return;
            }
        }

        try {
            Thread.sleep(new Random().nextInt(3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JandiPreference.setLastExecutedTime(System.currentTimeMillis());

        Intent serviceIntent = new Intent(getApplicationContext(), JandiDummyActivity.class);
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(serviceIntent);
    }
}
