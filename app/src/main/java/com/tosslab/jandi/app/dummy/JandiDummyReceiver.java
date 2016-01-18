package com.tosslab.jandi.app.dummy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.Subject;

/**
 * Created by tee on 15. 12. 17..
 */

// 프로세스가 죽을 경우 푸시를 받기 위한 더미 리시버
public class JandiDummyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())
                || Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
                || Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_RESTARTED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            Subject.just(1)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .delay(new Random().nextInt(3000), TimeUnit.MILLISECONDS)
                    .subscribe(i -> {
                        Intent serviceIntent = new Intent(context, JandiDummyActivity.class);
                        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(serviceIntent);
                        LogUtil.e("dummy activity running!!");
                    });
        }
    }
}
