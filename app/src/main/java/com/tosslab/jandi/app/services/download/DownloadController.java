package com.tosslab.jandi.app.services.download;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
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
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import java.io.File;
import java.util.Random;

/**
 * Created by tonyjs on 15. 11. 18..
 */
public class DownloadController {
    public static final String TAG = DownloadService.TAG;

    View view;

    private ResponseFuture<File> downloadTask;

    public DownloadController() {}

    public DownloadController(View view) {
        this.view = view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void onHandleIntent(Intent intent) {
        if (intent == null) {
            logUnknownException();
            view.showErrorToast(R.string.err_download);
            return;
        }

        final int fileId = intent.getIntExtra(DownloadService.KEY_FILE_ID, DownloadService.NONE_FILE_ID);
        final String fileUrl = intent.getStringExtra(DownloadService.KEY_FILE_URL);
        final String fileName = intent.getStringExtra(DownloadService.KEY_FILE_NAME);
        final String fileExt = intent.getStringExtra(DownloadService.KEY_FILE_EXTENSIONS);
        final String fileType = intent.getStringExtra(DownloadService.KEY_FILE_TYPE);

        if (!isValidateArguments(fileId, fileUrl, fileName, fileExt, fileType)) {
            logArgumentsException(fileId, fileUrl, fileName, fileExt, fileType);
            view.showErrorToast(R.string.err_download);
            return;
        }

        if (!isNetworkConnected()) {
            logNetworkIsNotConnected();
            view.showErrorToast(R.string.err_network);
            return;
        }

        // Network 상태 변경을 파악하기 위해 receiver 등록
        view.registerNetworkChangeReceiver();

        view.showToast(R.string.jandi_notify_download);

        File dir = makeDirIfNotExistsAndGet();

        File downloadTargetFile = getDownloadTargetFile(dir, fileName, fileExt);

        // '/download' path 가 Download Url 에 존재하지 않으면 붙여준다.
        String downloadUrl = getDownloadUrl(fileUrl);

        // 파일 하나당 한 개의 Notification 을 갖기 위해 random 으로 Notification Id 를 만든다.
        int notificationId = getNotificationId();

        NotificationCompat.Builder progressNotificationBuilder
                = view.getProgressNotificationBuilder(fileName);

        try {
            File file = downloadFileAndGet(downloadTargetFile,
                    downloadUrl, notificationId, progressNotificationBuilder);

            view.cancelNotification(notificationId);

            addToGalleryIfFileIsImage(file, fileType);

            Intent openFileViewerIntent = getFileViewerIntent(file, fileType);
            view.notifyComplete(fileName, notificationId, openFileViewerIntent);

            trackFileDownloadSuccess(fileId, fileType, fileExt, (int) file.length());
        } catch (Exception e) {
            logDownloadException(e);

            view.cancelNotification(notificationId);

            view.showErrorToast(R.string.err_download);
        }

        clear();
    }

    void logUnknownException() {
        LogUtil.e(TAG, "intent is empty.");
    }

    void logNetworkIsNotConnected() {
        LogUtil.e(TAG, "Network is not connected.");
    }

    boolean isValidateArguments(int fileId, String fileUrl, String fileName, String fileExt, String fileType) {
        return fileId != DownloadService.NONE_FILE_ID
                && !TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(fileName)
                && !TextUtils.isEmpty(fileExt) && !TextUtils.isEmpty(fileType);
    }

    void logArgumentsException(int fileId, String fileUrl, String fileName, String fileExt, String fileType) {
        String invalidateArgs =
                "Check your arguments - " +
                        "fileId(%d), fileUrl(%s), fileName(%s), fileExt(%s), fileType(%s)";
        LogUtil.e(TAG, String.format(invalidateArgs, fileId, fileUrl, fileName, fileExt, fileType));
    }

    public boolean isNetworkConnected() {
        return NetworkCheckUtil.isConnected();
    }

    File makeDirIfNotExistsAndGet() {
        File dir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    public String getDownloadUrl(String fileUrl) {
        boolean hasDownloadPath = fileUrl.lastIndexOf("/download") > 0;
        return hasDownloadPath ? fileUrl : fileUrl + "/download";
    }

    File getDownloadTargetFile(File dir, String fileName, String fileExt) {
        File file = new File(dir, FileUtil.getDownloadFileName(fileName, fileExt));
        if (file.exists()) {
            file = getDuplicatedFile(file);
        }
        return file;
    }

    /**
     * 같은 이름을 가진 파일이 이미 있는 경우 다음과 같이 만들어주기 위함
     * image.png > image(1).png
     */
    File getDuplicatedFile(File file) {
        String fileNameWithExt = file.getName();

        String[] split = fileNameWithExt.split("\\.");
        File dir = file.getParentFile();
        if (split.length <= 0) {
            return getDuplicatedFileWithoutExtensions(dir, fileNameWithExt);
        }

        StringBuilder sb = new StringBuilder();
        int untilFileNameLength = split.length - 1;
        for (int i = 0; i < untilFileNameLength; i++) {
            sb.append(split[i]);
            if (i < untilFileNameLength - 1) {
                sb.append("\\.");
            }
        }

        String fileName = sb.toString();
        String fileExtensions = split[untilFileNameLength];
        String newFileNameFormat = "%s(%d).%s";

        return getDuplicatedFile(dir, fileName, fileExtensions, newFileNameFormat);
    }

    private File getDuplicatedFile(File dir,
                                   String fileName, String fileExtensions, String newFileNameFormat) {
        File newFile;
        int duplicatedId = 1;
        while (true) {
            String newFileName = String.format(newFileNameFormat, fileName, duplicatedId, fileExtensions);
            newFile = new File(dir, newFileName);
            if (!newFile.exists()) {
                break;
            }
            duplicatedId++;
        }
        return newFile;
    }

    File getDuplicatedFileWithoutExtensions(File dir, String fileName) {
        File newFile;
        int duplicatedId = 1;
        while (true) {
            String newFileName = String.format("%s(%d)", fileName, duplicatedId);
            newFile = new File(dir, newFileName);
            if (!newFile.exists()) {
                break;
            }
            duplicatedId++;
        }
        return newFile;
    }

    int getNotificationId() {return Math.abs(new Random().nextInt());}

    public File downloadFileAndGet(File downloadTargetFile, String downloadUrl,
                            int notificationId, NotificationCompat.Builder progressNotificationBuilder)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        // Ion File download task - 인터넷이 끊긴 상황에서 cancel 하기 위해 field 로 활용
        downloadTask = buildDownloadTask(
                downloadTargetFile, downloadUrl, notificationId, progressNotificationBuilder);
        return downloadTask.get();
    }

    private ResponseFuture<File> buildDownloadTask(File downloadTargetFile, String downloadUrl,
                                                   int notificationId,
                                                   NotificationCompat.Builder progressNotificationBuilder) {

        return Ion.with(JandiApplication.getContext())
                .load(downloadUrl)
                .progressHandler((downloaded, total) -> {
                    view.notifyProgress(downloaded, total, notificationId, progressNotificationBuilder);
                })
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext()))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .write(downloadTargetFile);
    }

    void logDownloadException(Exception e) {LogUtil.e(TAG, Log.getStackTraceString(e));}

    synchronized void cancelDownload() {
        view.showErrorToast(R.string.err_network);
        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    private void clear() {
        downloadTask = null;
        view.unRegisterNetworkChangeReceiver();
        view.setLastNotificationTime(0);
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

        JandiApplication.getContext()
                .getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private boolean isMediaFile(String fileType) {
        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    void trackFileDownloadSuccess(int fileId, String fileType, String fileExt, int fileSize) {
        Context context = JandiApplication.getContext();
        Sprinkler.with(context)
                .track(new FutureTrack.Builder()
                        .event(Event.FileDownload)
                        .accountId(AccountUtil.getAccountId(context))
                        .memberId(AccountUtil.getMemberId(context))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.FileId, fileId)
                        .build());

        try {
            EntityManager entityManager = EntityManager.getInstance();

            MixpanelMemberAnalyticsClient mixpanelMemberAnalyticsClient =
                    MixpanelMemberAnalyticsClient.getInstance(context, entityManager.getDistictId());
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

    public interface View {
        void registerNetworkChangeReceiver();

        void unRegisterNetworkChangeReceiver();

        NotificationCompat.Builder getProgressNotificationBuilder(String fileName);

        void notifyProgress(long downloaded, long total,
                            int notificationId, NotificationCompat.Builder progressNotificationBuilder);

        void notifyComplete(String fileName, int notificationId, Intent openFileViewerIntent);

        void cancelNotification(int id);

        void setLastNotificationTime(long time);

        void showToast(int resId);

        void showErrorToast(int resId);
    }
}
