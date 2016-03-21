package com.tosslab.jandi.app.dummy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 15. 12. 17..
 */

// 프로세스가 죽을 경우 푸시를 받기 위한 더미 리시버
public class JandiDummyReceiver extends BroadcastReceiver {
    private static final String TAG = "JandiDummyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: " + this.getClass().getName());

        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())
                || Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
                || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            // delay가 random인 이유는 동시에 이벤트가 들어갔을때 액티비티 생명주기의 충돌을 방지하기 위함인듯..
            Observable.just(1)
                    .filter(val -> (System.currentTimeMillis() - JandiPreference.getLastExecutedTime()) < 1000 * 60 * 60 * 24)
                    .filter(it -> {
                        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
                        if (runningTasks != null
                                && runningTasks.size() > 0) {
                            String currentTopAppPackageName = runningTasks.get(0).topActivity.getPackageName();
                            if (TextUtils.equals(currentTopAppPackageName, context.getPackageName())) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .delay(new Random().nextInt(3000), TimeUnit.MILLISECONDS)
                    .subscribe(i -> {
                        JandiPreference.setLastExecutedTime(System.currentTimeMillis());
                        Intent serviceIntent = new Intent(context, JandiDummyActivity.class);
                        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(serviceIntent);
                    });

        }
    }
}