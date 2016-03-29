package com.tosslab.jandi.app.services.upload;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UploadStopBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int stopId = intent.getIntExtra(FileUploadManager.EXTRA_STOP_ID, -1);

        if (stopId <= 0) {
            return;
        }


        FileUploadManager uploadManager = FileUploadManager.getInstance();
        uploadManager.cancelAll();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(stopId);
    }
}
