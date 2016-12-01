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

import rx.Observable;

public class DownloadController {
    public static final String TAG = DownloadService.TAG;

    View view;

    private FileDownloadApi fileDownloadApi;

    public DownloadController() {
        fileDownloadApi = new FileDownloadApi();
    }

    public DownloadController(View view) {
        this();
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
            view.prepareProgress(notificationId, progressNotificationBuilder);

            File downloadedFile
                    = fileDownloadApi.downloadImmediatly(downloadUrl,
                    downloadTargetFile.getAbsolutePath()
                    , callback -> callback
                            .onBackpressureBuffer()
                            .distinctUntilChanged()
                            .subscribe(it -> view.notifyProgress(it, 100, notificationId, progressNotificationBuilder),
                                    t -> t.printStackTrace(),
                                    () -> {
                                        DownloadController.this.registDownloadInfo(downloadFileInfo, downloadTargetFile);
                                        DownloadModel.addToGalleryIfFileIsImage(downloadTargetFile, downloadFileInfo.getFileType());
                                        Intent openFileViewerIntent = DownloadController.this.getFileViewerIntent(downloadTargetFile, downloadFileInfo.getFileType());
                                        view.notifyComplete(downloadFileInfo.getFileName(), notificationId, openFileViewerIntent);
                                        SprinklrFileDownload.sendLog(downloadFileInfo.getFileId());
                                        DownloadModel.deleteDownloadInfo(notificationId);
                                        DownloadController.this.clear();
                                    }));

            if (downloadedFile == null) {
                cancelDownload(notificationId);
            }
        } catch (Exception e) {
            DownloadModel.logDownloadException(e);
            if (!(e.getCause() instanceof CancellationException)) {
                view.showErrorToast(R.string.err_download);
            }
            cancelDownload(notificationId);
            return;
        }
    }

    private void cancelDownload(int notificationId) {
        view.cancelNotification(notificationId);
        SprinklrFileDownload.sendFailLog(-1);
        DownloadModel.deleteDownloadInfo(notificationId);
        clear();
    }

    // 특정 단말기(Ex. SAMSUNG)의 다운로드 기록에 보여지기 위한 코드
    private void registDownloadInfo(DownloadFileInfo downloadFileInfo, File downloadTargetFile) {
        String name = downloadTargetFile.getName();
        String description = downloadTargetFile.getAbsolutePath();
        String fileType = downloadFileInfo.getFileType();
        long length = downloadTargetFile.length();
        DownloadManager downloadManager =
                (DownloadManager) JandiApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.addCompletedDownload(name, description,
                true, fileType, downloadTargetFile.getAbsolutePath(), length, false);
    }

    synchronized void cancelDownload() {
        if (fileDownloadApi != null) {
            fileDownloadApi.cancel();
        }
    }

    private void clear() {
        fileDownloadApi = null;
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

        void prepareProgress(int notificationId, NotificationCompat.Builder progressNotificationBuilder);

        void notifyProgress(long downloaded, long total,
                            int notificationId, NotificationCompat.Builder progressNotificationBuilder);

        void notifyComplete(String fileName, int notificationId, Intent openFileViewerIntent);

        void cancelNotification(int id);

        void setLastNotificationTime(long time);

        void showToast(int resId);

        void showErrorToast(int resId);
    }

}
