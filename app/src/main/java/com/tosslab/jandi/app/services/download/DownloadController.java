package com.tosslab.jandi.app.services.download;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.DownloadInfo;
import com.tosslab.jandi.app.local.orm.repositories.DownloadRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.services.download.domain.DownloadFileInfo;
import com.tosslab.jandi.app.services.download.model.DownloadModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import java.io.File;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 15. 11. 18..
 */
public class DownloadController {
    public static final String TAG = DownloadService.TAG;

    View view;

    private ResponseFuture<File> downloadTask;

    public DownloadController() {
    }

    public DownloadController(View view) {
        this.view = view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void onHandleIntent(Intent intent) {

        if (DownloadModel.isRestart()) {

            List<DownloadInfo> downloadInfosInProgress = DownloadRepository.getInstance().getDownloadInfosInProgress();

            Observable.from(downloadInfosInProgress)
                    .map(DownloadInfo::getNotificationId)
                    .subscribe(notificationId -> {
                        view.cancelNotification(notificationId);
                        DownloadModel.deleteDownloadInfo(notificationId);
                    });
            view.showErrorToast(R.string.err_download);

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
                = view.getProgressNotificationBuilder(downloadFileInfo.getFileName());

        try {
            File file = downloadFileAndGet(downloadTargetFile,
                    downloadUrl, (downloaded, total) -> {
                        view.notifyProgress(downloaded,
                                total,
                                notificationId,
                                progressNotificationBuilder);
                    });


            view.cancelNotification(notificationId);

            DownloadModel.addToGalleryIfFileIsImage(file, downloadFileInfo.getFileType());

            Intent openFileViewerIntent = getFileViewerIntent(file, downloadFileInfo.getFileType());
            view.notifyComplete(downloadFileInfo.getFileName(), notificationId, openFileViewerIntent);

            trackFileDownloadSuccess(downloadFileInfo.getFileId(),
                    downloadFileInfo.getFileType(),
                    downloadFileInfo.getFileExt(),
                    (int) file.length());
        } catch (Exception e) {
            DownloadModel.logDownloadException(e);

            view.cancelNotification(notificationId);

            view.showErrorToast(R.string.err_download);
        }

        DownloadModel.deleteDownloadInfo(notificationId);

        clear();
    }

    File downloadFileAndGet(File downloadTargetFile, String downloadUrl,
                            ProgressCallback downloadCallback)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        // Ion File download task - 인터넷이 끊긴 상황에서 cancel 하기 위해 field 로 활용
        downloadTask = DownloadModel.buildDownloadTask(downloadTargetFile, downloadUrl, downloadCallback);
        return downloadTask.get();
    }

    synchronized void cancelDownload() {

        if (downloadTask != null) {
            downloadTask.cancel();
        }
    }

    private void clear() {
        downloadTask = null;
        view.unRegisterNetworkChangeReceiver();
        view.setLastNotificationTime(0);
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
