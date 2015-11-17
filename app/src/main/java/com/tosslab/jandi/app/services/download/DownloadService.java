package com.tosslab.jandi.app.services.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.file.FileSizeUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import java.io.File;
import java.util.Random;

/**
 * Created by tonyjs on 15. 11. 17..
 */
public class DownloadService extends IntentService {
    public static final String TAG = DownloadService.class.getSimpleName();
    private static final String KEY_FILE_ID = "file_id";
    private static final String KEY_FILE_URL = "url";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_FILE_EXTENSIONS = "ext";
    private static final String KEY_FILE_TYPE = "file_type";

    private NotificationManager notificationManager;

    // 노티피케이션을 자주 업데이트 하는 경우 시스템 렉이 엄청 걸림. 초단위로 보여주기 위한 마지막 타임스탬프
    private long lastNotificationTime;

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
        lastNotificationTime = 0;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isValidateArguments(intent)) {
            return;
        }

        int fileId = intent.getIntExtra(KEY_FILE_ID, -1);
        String url = intent.getStringExtra(KEY_FILE_URL);
        String fileName = intent.getStringExtra(KEY_FILE_NAME);
        String ext = intent.getStringExtra(KEY_FILE_EXTENSIONS);
        String fileType = intent.getStringExtra(KEY_FILE_TYPE);

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        boolean hasDownloadPath = url.lastIndexOf("/download") > 0;
        String downloadUrl = hasDownloadPath ? url : url + "/download";

        int notificationId = Math.abs(new Random().nextInt());
        NotificationCompat.Builder progressNotificationBuilder = getProgressNotificationBuilder(fileName);

        File file = downloadFile(
                downloadUrl, dir, fileName, ext, notificationId, progressNotificationBuilder);

        if (file == null) {
            notificationManager.cancel(notificationId);
            return;
        }

        addToGalleryIfFileIsImage(file, fileType);

        notifyComplete(file, fileName, fileType, notificationId);

        trackFileDownloadSuccess(fileId);
    }

    private boolean isValidateArguments(Intent intent) {
        return intent != null
                && intent.getIntExtra(KEY_FILE_ID, -1) != -1
                && !TextUtils.isEmpty(intent.getStringExtra(KEY_FILE_URL))
                && !TextUtils.isEmpty(intent.getStringExtra(KEY_FILE_NAME))
                && !TextUtils.isEmpty(intent.getStringExtra(KEY_FILE_EXTENSIONS));
    }

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

    private File downloadFile(String downloadUrl, File dir, String fileName, String ext,
                              final int notificationId,
                              final NotificationCompat.Builder progressNotificationBuilder) {
        File file = null;
        try {
            file = Ion.with(JandiApplication.getContext())
                    .load(downloadUrl)
                    .progressHandler((downloaded, total) -> {
                        showProgress(downloaded, total, notificationId, progressNotificationBuilder);
                    })
                    .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext()))
                    .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                    .write(new File(dir, FileSizeUtil.getDownloadFileName(fileName, ext)))
                    .get();
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
        lastNotificationTime = 0;
        return file;
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

    void addToGalleryIfFileIsImage(File image, String fileType) {
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

    boolean isMediaFile(String fileType) {
        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    void trackFileDownloadSuccess(int fileId) {
        Sprinkler.with(getApplicationContext())
                .track(new FutureTrack.Builder()
                        .event(Event.FileDownload)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.FileId, fileId)
                        .build());
    }

    Intent getFileViewerIntent(File file, String fileType) {
        if (TextUtils.isEmpty(fileType)) {
            return null;
        }

        return FileOpenDelegator.getIntent(file, fileType);
    }

    void notifyComplete(File file, String fileName, String fileType, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(fileName);
        builder.setTicker("DownloadComplete !!");
        builder.setContentText("DownloadComplete !!");
        builder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
        builder.setAutoCancel(true);

        Intent intent = getFileViewerIntent(file, fileType);
        if (intent != null) {
            builder.setContentIntent(PendingIntent.getBroadcast(
                    getApplicationContext(), 20151117, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, builder.build());
    }
}
