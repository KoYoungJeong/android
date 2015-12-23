package com.tosslab.jandi.app.services.download.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tosslab.jandi.app.services.download.DownloadService;

public class DownloadStopProxyBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.tosslab.jandi.app.download.service.proxy.receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent stopIntent = new Intent(DownloadService.ACTION_DOWNLOAD_SERVICE);
        stopIntent.putExtra(DownloadService.EXTRA_NOTIFICATION_ID, intent.getIntExtra(DownloadService.EXTRA_NOTIFICATION_ID, -1));
        context.sendOrderedBroadcast(stopIntent, null);
    }
}
