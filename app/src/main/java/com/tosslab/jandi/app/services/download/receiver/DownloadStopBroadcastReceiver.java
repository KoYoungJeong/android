package com.tosslab.jandi.app.services.download.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.services.download.model.DownloadModel;

public class DownloadStopBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra(DownloadService.EXTRA_NOTIFICATION_ID)) {
            int notificationId = intent.getIntExtra(DownloadService.EXTRA_NOTIFICATION_ID, -1);
            if (notificationId > 0) {
                DownloadModel.deleteDownloadInfo(notificationId);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);
            }
        }
    }
}
