package com.tosslab.jandi.app.services.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.json.JSONException;

import java.io.File;
import java.util.Random;

/**
 * Created by tonyjs on 15. 11. 17..
 */
public class DownloadService extends IntentService {
    public static final String TAG = DownloadService.class.getSimpleName();

    public static void start(int fileId, String url, String fileName, String ext, String fileType) {
        Intent intent = new Intent(JandiApplication.getContext(), DownloadService.class);
        intent.putExtra(KEY_FILE_ID, fileId);
        intent.putExtra(KEY_FILE_URL, url);
        intent.putExtra(KEY_FILE_NAME, fileName);
        intent.putExtra(KEY_FILE_EXTENSIONS, ext);
        intent.putExtra(KEY_FILE_TYPE, fileType);
        JandiApplication.getContext().startService(intent);
    }

    private static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private static final String KEY_FILE_ID = "file_id";
    private static final String KEY_FILE_URL = "url";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_FILE_EXTENSIONS = "ext";
    private static final String KEY_FILE_TYPE = "file_type";

    private NotificationManager notificationManager;

    private Handler uiHandler;

    private ResponseFuture<File> downloadTask;

    private BroadcastReceiver networkChangeBroadcastReceiver = new BroadcastReceiver() {
        private boolean isRegister = true;
        private boolean isHandled = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRegister) {
                isRegister = false;
                return;
            }

            if (isHandled) {
                return;
            }

            LogUtil.d(TAG, String.format("Intent - %s, extras - %s", intent.toString(), intent.getExtras().toString()));
            boolean connected = isNetworkConnected();
            LogUtil.i(TAG, "NetworkChanged - " + connected);
            if (!connected) {
                cancelDownload();
                isHandled = true;
            }
        }
    };

    // 노티피케이션을 자주 업데이트 하는 경우 시스템 렉이 엄청 걸림. 초단위로 보여주기 위한 마지막 타임스탬프
    private long lastNotificationTime;

    public DownloadService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        lastNotificationTime = 0;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isValidateArguments(intent)) {
            return;
        }

        if (!isNetworkConnected()) {
            showErrorToast(getString(R.string.err_network));
            return;
        }

        // Network 상태 변경을 파악하기 위해 receiver 등록
        IntentFilter filter = new IntentFilter(ACTION_CONNECTIVITY_CHANGE);
        registerReceiver(networkChangeBroadcastReceiver, filter);

        int fileId = intent.getIntExtra(KEY_FILE_ID, -1);
        String url = intent.getStringExtra(KEY_FILE_URL);
        String fileName = intent.getStringExtra(KEY_FILE_NAME);
        String ext = intent.getStringExtra(KEY_FILE_EXTENSIONS);
        String fileType = intent.getStringExtra(KEY_FILE_TYPE);

        // Download dir
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File downloadTargetFile = new File(dir, FileUtil.getDownloadFileName(fileName, ext));
        if (downloadTargetFile.exists()) {
            //FIXME
        }

        // '/download' path 가 Download Url 에 존재하지 않으면 붙여준다.
        boolean hasDownloadPath = url.lastIndexOf("/download") > 0;
        String downloadUrl = hasDownloadPath ? url : url + "/download";

        // 파일 하나당 한 개의 Notificatinon 을 갖기 위해 random 으로 Notification Id 를 만든다.
        int notificationId = Math.abs(new Random().nextInt());
        NotificationCompat.Builder progressNotificationBuilder = getProgressNotificationBuilder(fileName);

        // Ion File download task - 인터넷이 끊긴 상황에서 cancel 하기 위해 field 로 활용
        downloadTask = buildDownloadTask(
                downloadTargetFile, downloadUrl, notificationId, progressNotificationBuilder);

        File file;
        try {
            file = downloadTask.get();

            notificationManager.cancel(notificationId);

            addToGalleryIfFileIsImage(file, fileType);

            Intent openFileViewerIntent = getFileViewerIntent(file, fileType);
            notifyComplete(fileName, notificationId, openFileViewerIntent);

            trackFileDownloadSuccess(fileId, fileType, ext, (int) file.length());
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            notificationManager.cancel(notificationId);
        }

        clear();
    }

    private boolean isValidateArguments(Intent intent) {
        return intent != null
                && intent.getIntExtra(KEY_FILE_ID, -1) != -1
                && !TextUtils.isEmpty(intent.getStringExtra(KEY_FILE_URL))
                && !TextUtils.isEmpty(intent.getStringExtra(KEY_FILE_NAME))
                && !TextUtils.isEmpty(intent.getStringExtra(KEY_FILE_EXTENSIONS));
    }

    private boolean isNetworkConnected() {return NetworkCheckUtil.isConnected();}

    private NotificationCompat.Builder getProgressNotificationBuilder(String fileName) {
        NotificationCompat.Builder progressNotificationBuilder = new NotificationCompat.Builder(this);
        progressNotificationBuilder.setWhen(System.currentTimeMillis());
        progressNotificationBuilder.setOngoing(true);
        progressNotificationBuilder.setTicker(getString(R.string.app_name));
        progressNotificationBuilder.setContentText(getString(R.string.app_name));
        progressNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        progressNotificationBuilder.setContentTitle(fileName);
        return progressNotificationBuilder;
    }

    private ResponseFuture<File> buildDownloadTask(File downloadTargetFile, String downloadUrl,
                                                   int notificationId,
                                                   NotificationCompat.Builder progressNotificationBuilder) {

        return Ion.with(JandiApplication.getContext())
                .load(downloadUrl)
                .progressHandler((downloaded, total) -> {
                    showProgress(downloaded, total, notificationId, progressNotificationBuilder);
                })
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext()))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .write(downloadTargetFile);
    }

    private void showProgress(long downloaded, long total,
                              int notificationId, NotificationCompat.Builder progressNotificationBuilder) {
        int progress = (int) (downloaded * 100 / total);

        if (System.currentTimeMillis() - lastNotificationTime >= 1000) {
            progressNotificationBuilder.setProgress(100, progress, false);
            notificationManager.notify(notificationId, progressNotificationBuilder.build());
            lastNotificationTime = System.currentTimeMillis();
        }
    }

    private void showErrorToast(String message) {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
        uiHandler.post(() -> ColoredToast.showError(getApplicationContext(), message));
    }

    private synchronized void cancelDownload() {
        showErrorToast(getString(R.string.err_network));
        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    private void clear() {
        unregisterReceiver(networkChangeBroadcastReceiver);
        downloadTask = null;
        lastNotificationTime = 0;
    }

    private void addToGalleryIfFileIsImage(File image, String fileType) {
        if (!isMediaFile(fileType)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, image.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, image.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, fileType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());

        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private boolean isMediaFile(String fileType) {
        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    private void trackFileDownloadSuccess(int fileId, String fileType, String fileExt, int fileSize) {
        Sprinkler.with(getApplicationContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileDownload)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.FileId, fileId)
                        .build());

        try {
            EntityManager entityManager = EntityManager.getInstance();

            MixpanelMemberAnalyticsClient mixpanelMemberAnalyticsClient =
                    MixpanelMemberAnalyticsClient.getInstance(
                            getApplicationContext(), entityManager.getDistictId());
            mixpanelMemberAnalyticsClient.trackDownloadFile(fileType, fileExt, fileSize);

        } catch (Exception e) {
            LogUtil.e(TAG, "Mixpanel exception has occurred. - " + Log.getStackTraceString(e));
        }

    }

    private Intent getFileViewerIntent(File file, String fileType) {
        if (TextUtils.isEmpty(fileType)) {
            return null;
        }

        return FileOpenDelegator.getIntent(file, fileType);
    }

    void notifyComplete(String fileName, int notificationId, Intent openFileViewerIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(fileName);
        builder.setTicker("DownloadComplete !!");
        builder.setContentText("DownloadComplete !!");
        builder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
        builder.setAutoCancel(true);
        if (openFileViewerIntent != null) {
            builder.setContentIntent(PendingIntent.getBroadcast(
                    getApplicationContext(), 20151117, openFileViewerIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, builder.build());
    }
}
