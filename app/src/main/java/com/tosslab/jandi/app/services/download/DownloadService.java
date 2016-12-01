package com.tosslab.jandi.app.services.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.download.model.DownloadModel;
import com.tosslab.jandi.app.services.download.receiver.DownloadStopProxyBroadcastReceiver;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 11. 17..
 */
public class DownloadService extends IntentService implements DownloadController.View {
    public static final String TAG = DownloadService.class.getSimpleName();
    public static final String KEY_FILE_ID = "file_id";
    public static final String KEY_FILE_URL = "url";
    public static final String KEY_FILE_NAME = "file_name";
    public static final String KEY_FILE_EXTENSIONS = "ext";
    public static final String KEY_FILE_TYPE = "file_type";
    public static final long NONE_FILE_ID = -1;
    public static final String ACTION_STOP_DOWNLOAD_SERVICE = "com.tosslab.jandi.app.download.service.stop";
    public static final String EXTRA_STOP = "stop";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    private static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private DownloadController downloadController;
    private NotificationManager notificationManager;
    private Handler uiHandler;
    // 노티피케이션을 자주 업데이트 하는 경우 시스템 렉이 엄청 걸림. 초단위로 보여주기 위한 마지막 타임스탬프
    private long lastNotificationTime;

    private BroadcastReceiver networkChangeBroadcastReceiver = new BroadcastReceiver() {
        private boolean isRegister = true;
        private boolean isHandled = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRegister) {
                isRegister = false;
                return;
            }

            if (isHandled || downloadController == null) {
                return;
            }

            boolean connected = DownloadModel.isNetworkConnected();
            if (!connected) {
                showErrorToast(R.string.err_network);
                downloadController.cancelDownload();
                isHandled = true;
            }
        }
    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (downloadController != null) {
                downloadController.cancelDownload();
                abortBroadcast();
            }
        }
    };

    private boolean isRedeliveried;

    public DownloadService() {
        super(TAG);
    }

    public static void start(long fileId, String url, String fileName, String ext, String fileType) {
        Intent intent = new Intent(JandiApplication.getContext(), DownloadService.class);
        intent.putExtra(KEY_FILE_ID, fileId);
        intent.putExtra(KEY_FILE_URL, url);
        intent.putExtra(KEY_FILE_NAME, fileName);
        intent.putExtra(KEY_FILE_EXTENSIONS, ext);
        intent.putExtra(KEY_FILE_TYPE, fileType);
        JandiApplication.getContext().startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setLastNotificationTime(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((flags & Service.START_REDELIVER_INTENT) == 0) {
            setIntentRedelivery(true);
            isRedeliveried = false;
        } else {
            setIntentRedelivery(false);
            isRedeliveried = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        downloadController = new DownloadController(this);
        downloadController.onHandleIntent(intent, isRedeliveried);
    }

    @Override
    public void onDestroy() {
        LogUtil.e("onDestroy");
        clear();
        super.onDestroy();
    }

    void clear() {
        unRegisterNetworkChangeReceiver();

        if (downloadController != null) {
            downloadController = null;
        }

        if (notificationManager != null) {
            notificationManager = null;
        }

        if (uiHandler != null) {
            uiHandler = null;
        }
    }

    @Override
    public void registerNetworkChangeReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_CONNECTIVITY_CHANGE);
        registerReceiver(networkChangeBroadcastReceiver, filter);

        IntentFilter stopFilter = new IntentFilter(ACTION_STOP_DOWNLOAD_SERVICE);
        stopFilter.setPriority(1);
        registerReceiver(stopReceiver, stopFilter);
    }

    @Override
    public void unRegisterNetworkChangeReceiver() {
        try {
            unregisterReceiver(networkChangeBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(stopReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NotificationCompat.Builder getProgressNotificationBuilder(int notificationId, String fileName) {
        NotificationCompat.Builder progressNotificationBuilder = new NotificationCompat.Builder(this);
        Context context = JandiApplication.getContext();
        Intent intent = new Intent(DownloadStopProxyBroadcastReceiver.ACTION);
        intent.putExtra(EXTRA_STOP, true);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent actionCancelIntent = PendingIntent.getBroadcast(context, 121, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        progressNotificationBuilder
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setTicker(fileName)
                .setContentTitle(fileName)
                .setContentText(getString(R.string.app_name))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.jandi_cancel), actionCancelIntent)
                .setSmallIcon(android.R.drawable.stat_sys_download);
        return progressNotificationBuilder;
    }

    @Override
    public void notifyProgress(long downloaded, long total,
                               int notificationId, NotificationCompat.Builder progressNotificationBuilder) {
        int progress = (int) (downloaded * 100 / total);
        if (System.currentTimeMillis() - lastNotificationTime >= 1000) {
            progressNotificationBuilder.setProgress(100, progress, false);
            notificationManager.notify(notificationId, progressNotificationBuilder.build());
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    @Override
    public void prepareProgress(int notificationId, NotificationCompat.Builder progressNotificationBuilder) {
        progressNotificationBuilder.setProgress(0, 0, false);
        notificationManager.notify(notificationId, progressNotificationBuilder.build());
    }

    @Override
    public void notifyComplete(String fileName, int notificationId, Intent openFileViewerIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(fileName)
                .setTicker(fileName)
                .setContentTitle(fileName)
                .setContentText(getString(R.string.jandi_notify_download_complete))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setSmallIcon(android.R.drawable.stat_sys_download_done);
        if (openFileViewerIntent != null) {
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(JandiApplication.getContext(),
                            20151117, openFileViewerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
        }

        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    @Override
    public void setLastNotificationTime(long time) {
        lastNotificationTime = time;
    }

    @Override
    public void showToast(int resId) {
        initializeUiHandler();
        uiHandler.post(() -> ColoredToast.show(getString(resId)));
    }

    @Override
    public void showErrorToast(int resId) {
        initializeUiHandler();
        uiHandler.post(() -> ColoredToast.showError(getString(resId)));
    }

    void initializeUiHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
    }

}
