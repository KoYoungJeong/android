package com.tosslab.jandi.app.services.download;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.domain.DownloadInfo;
import com.tosslab.jandi.app.local.orm.repositories.DownloadRepository;
import com.tosslab.jandi.app.network.file.FileDownloadApi;
import com.tosslab.jandi.app.services.download.domain.DownloadFileInfo;
import com.tosslab.jandi.app.services.download.model.DownloadModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrFileDownload;

import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class DownloadController {
    public static final String TAG = DownloadService.TAG;

    View view;

    private Call<ResponseBody> downloadTask;

    public DownloadController() {
    }

    public DownloadController(View view) {
        this.view = view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void onHandleIntent(Intent intent, boolean isRedeliveried) {

        if (isRedeliveried) {

            List<DownloadInfo> downloadInfosInProgress = DownloadRepository.getInstance().getDownloadInfosInProgress();

            if (!(downloadInfosInProgress.isEmpty())) {

                Observable.from(downloadInfosInProgress)
                        .map(DownloadInfo::getNotificationId)
                        .subscribe(notificationId -> {
                            view.cancelNotification(notificationId);
                            DownloadModel.deleteDownloadInfo(notificationId);
                        });

                view.showErrorToast(R.string.err_download);
            }


            return;
        }

        if (intent == null) {
            DownloadModel.logUnknownException();
            view.showErrorToast(R.string.err_download);
            return;
        }

        DownloadFileInfo downloadFileInfo = DownloadModel.getDownloadInfo(intent);

        if (!DownloadModel.isValidateArguments(downloadFileInfo)) {
            DownloadModel.logArgumentsException(downloadFileInfo);
            view.showErrorToast(R.string.err_download);
            return;
        }

        if (!DownloadModel.isNetworkConnected()) {
            DownloadModel.logNetworkIsNotConnected();
            view.showErrorToast(R.string.err_network);
            return;
        }

        // Network 상태 변경을 파악하기 위해 receiver 등록
        view.registerNetworkChangeReceiver();

        view.showToast(R.string.jandi_notify_download);

        File dir = DownloadModel.makeDirIfNotExistsAndGet();

        File downloadTargetFile = DownloadModel.getDownloadTargetFile(dir,
                downloadFileInfo.getFileName(),
                downloadFileInfo.getFileExt());

        // '/download' path 가 Download Url 에 존재하지 않으면 붙여준다.
        String downloadUrl = DownloadModel.getDownloadUrl(downloadFileInfo.getFileUrl());

        // 파일 하나당 한 개의 Notification 을 갖기 위해 random 으로 Notification Id 를 만든다.
        int notificationId = DownloadModel.getNotificationId();

        DownloadModel.upsertDownloadInfo(notificationId, downloadFileInfo.getFileName(), 0);


        NotificationCompat.Builder progressNotificationBuilder
                = view.getProgressNotificationBuilder(notificationId, downloadFileInfo.getFileName());

        try {
            File file = downloadFileAndGet(downloadTargetFile,
                    downloadUrl,
                    callback -> callback.distinctUntilChanged()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(it -> {
                                view.notifyProgress(it, 100, notificationId, progressNotificationBuilder);
                            }, t -> {
                            }));


            String name = file.getName();
            String description = file.getAbsolutePath();
            String fileType = downloadFileInfo.getFileType();
            long length = file.length();

            addDownloadManager(name, description, fileType, file.getAbsolutePath(), length);

            DownloadModel.addToGalleryIfFileIsImage(file, downloadFileInfo.getFileType());

            Intent openFileViewerIntent = getFileViewerIntent(file, downloadFileInfo.getFileType());
            view.notifyComplete(downloadFileInfo.getFileName(), notificationId, openFileViewerIntent);

            SprinklrFileDownload.sendLog(downloadFileInfo.getFileId());
        } catch (Exception e) {
            DownloadModel.logDownloadException(e);
            view.cancelNotification(notificationId);

            if (!(e.getCause() instanceof CancellationException)) {
                view.showErrorToast(R.string.err_download);
            }
            SprinklrFileDownload.sendFailLog(-1);

        }

        DownloadModel.deleteDownloadInfo(notificationId);

        clear();
    }

    private void addDownloadManager(String name, String description,
                                    String fileType, String filePath, long length) {
        DownloadManager downloadManager =
                (DownloadManager) JandiApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.addCompletedDownload(name, description,
                true, fileType, filePath, length, false);
    }

    File downloadFileAndGet(File downloadTargetFile, String downloadUrl,
                            com.tosslab.jandi.app.network.file.body.ProgressCallback callback) {
        // Ion File download task - 인터넷이 끊긴 상황에서 cancel 하기 위해 field 로 활용
        downloadTask = new FileDownloadApi().download(downloadUrl, downloadTargetFile.getAbsolutePath(), callback);
        return downloadTargetFile;
    }

    synchronized void cancelDownload() {

        if (downloadTask != null && downloadTask.isExecuted()) {
            downloadTask.cancel();
        }
    }

    private void clear() {
        downloadTask = null;
        view.unRegisterNetworkChangeReceiver();
        view.setLastNotificationTime(0);
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

        NotificationCompat.Builder getProgressNotificationBuilder(int notificationId, String fileName);

        void notifyProgress(long downloaded, long total,
                            int notificationId, NotificationCompat.Builder progressNotificationBuilder);

        void notifyComplete(String fileName, int notificationId, Intent openFileViewerIntent);

        void cancelNotification(int id);

        void setLastNotificationTime(long time);

        void showToast(int resId);

        void showErrorToast(int resId);

    }
}
