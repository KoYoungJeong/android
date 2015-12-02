package com.tosslab.jandi.app.services.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 11. 17..
 */
public class DownloadService extends IntentService implements DownloadController.View {
    public static final String TAG = DownloadService.class.getSimpleName();
    static final String KEY_FILE_ID = "file_id";
    static final String KEY_FILE_URL = "url";
    static final String KEY_FILE_NAME = "file_name";
    static final String KEY_FILE_EXTENSIONS = "ext";
    static final String KEY_FILE_TYPE = "file_type";
    static final int NONE_FILE_ID = -1;
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

            boolean connected = downloadController.isNetworkConnected();
            LogUtil.i(TAG, "NetworkChanged - " + connected);
            if (!connected) {
                downloadController.cancelDownload();
                isHandled = true;
            }
        }
    };

    public DownloadService() {
        super(TAG);
    }

    public static void start(int fileId, String url, String fileName, String ext, String fileType) {
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
    protected void onHandleIntent(Intent intent) {
        downloadController = new DownloadController(this);
        downloadController.onHandleIntent(intent);
    }

    @Override
    public void onDestroy() {
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
    }

    @Override
    public void unRegisterNetworkChangeReceiver() {
        try {
            unregisterReceiver(networkChangeBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NotificationCompat.Builder getProgressNotificationBuilder(String fileName) {
        NotificationCompat.Builder progressNotificationBuilder = new NotificationCompat.Builder(this);
        progressNotificationBuilder
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setTicker(fileName)
                .setContentTitle(fileName)
                .setContentText(getString(R.string.app_name))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
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
        uiHandler.post(() -> ColoredToast.show(getApplicationContext(), getString(resId)));
    }

    @Override
    public void showErrorToast(int resId) {
        initializeUiHandler();
        uiHandler.post(() -> ColoredToast.showError(getApplicationContext(), getString(resId)));
    }

    void initializeUiHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
    }

}
