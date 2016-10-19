package com.tosslab.jandi.app.services.upload;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.FileUploadProgressEvent;
import com.tosslab.jandi.app.events.files.FileUploadStartEvent;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel_;
import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.file.FileUploadApi;
import com.tosslab.jandi.app.network.file.body.ProgressCallback;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadManager {

    public static final String EXTRA_STOP_ID = "stop_id";
    private static final String EXTRA_STOP = "stop";
    private static FileUploadManager manager;
    private PublishSubject<FileUploadDTO> objectPublishSubject;
    private Call<ResUploadedFile> lastRequest;
    private List<FileUploadDTO> fileUploadDTOs;
    private int notificationId = 100;
    private NotificationCompat.Builder notificationBuilder;
    private Subscription subscription;
    private PendingIntent pendingIntent;

    private FileUploadManager() {
        Context context = JandiApplication.getContext();
        fileUploadDTOs = new CopyOnWriteArrayList<>();

        notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setTicker(JandiApplication.getContext().getString(R.string.title_file_upload))
                .setContentTitle(JandiApplication.getContext().getString(R.string.title_file_upload))
                .setContentText(JandiApplication.getContext().getString(R.string.app_name))
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setSmallIcon(android.R.drawable.stat_sys_upload);

        objectPublishSubject = PublishSubject.create();
        initSubscription(context);
    }

    public static FileUploadManager getInstance() {
        if (manager == null) {
            manager = new FileUploadManager();
        }
        return manager;
    }

    private static Call<ResUploadedFile> uploadFile(String realFilePath,
                                                    boolean isPublicTopic,
                                                    String title, long teamId, long entityId,
                                                    String comment, List<MentionObject> mentions,
                                                    ProgressCallback progressCallback) {
        File uploadFile = new File(realFilePath);
        String permissionCode = (isPublicTopic) ? "744" : "740";

        FileUploadApi fileUploadApi = new FileUploadApi();
        return fileUploadApi.uploadFile(title, entityId, permissionCode, teamId, comment, mentions, uploadFile, progressCallback);

    }

    private void initSubscription(Context context) {
        subscription = objectPublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.newThread())
                .subscribe(fileUploadDTO -> {

                    notificationBuilder.setProgress(100, 0, false)
                            .setAutoCancel(false)
                            .setContentTitle(JandiApplication.getContext().getString(R.string.jandi_file_upload_state, String.valueOf((fileUploadDTOs.indexOf(fileUploadDTO) + 1)), String.valueOf(fileUploadDTOs.size())))
                            .setContentIntent(getPendingActivities(context, fileUploadDTO))
                            .setSmallIcon(android.R.drawable.stat_sys_upload);
                    updateNotificationBuilder();
                    showNotification(context);

                    final int[] progress = {0, 0};
                    fileUploadDTO.setUploadState(FileUploadDTO.UploadState.PROGRESS);
                    EventBus.getDefault().post(new FileUploadStartEvent(fileUploadDTO.getEntity()));
                    FilePickerModel filePickerModel = FilePickerModel_.getInstance_(context);

                    boolean isPublicTopic = filePickerModel.isPublicEntity(fileUploadDTO.getEntity());

                    Call<ResUploadedFile> request = uploadFile(fileUploadDTO.getFilePath(),
                            isPublicTopic, fileUploadDTO.getFileName(),
                            fileUploadDTO.getTeamId(),
                            fileUploadDTO.getEntity(),
                            fileUploadDTO.getComment(),
                            fileUploadDTO.getMentions(),
                            callback -> callback
                                    .onBackpressureBuffer()
                                    .distinctUntilChanged()
                                    .subscribe(it -> {
                                        progress[1] = it;
                                        FileUploadProgressEvent event = new FileUploadProgressEvent(fileUploadDTO.getEntity(), it);
                                        EventBus.getDefault().post(event);
                                        if (progress[0] != progress[1]) {
                                            progress[0] = progress[1];
                                            notificationBuilder.setContentTitle(JandiApplication.getContext().getString(R.string.jandi_file_upload_state, String.valueOf((fileUploadDTOs.indexOf(fileUploadDTO) + 1)), String.valueOf(fileUploadDTOs.size())))
                                                    .setContentText(JandiApplication.getContext().getString(R.string.app_name));
                                            notificationBuilder.setProgress(100, progress[0], false);
                                            showNotification(context);
                                        }
                                    }, t -> {
                                        fileUploadDTO.setUploadState(FileUploadDTO.UploadState.FAIL);
                                    }, () -> {
                                        fileUploadDTO.setUploadState(FileUploadDTO.UploadState.SUCCESS);
                                    }));
                    lastRequest = request;

                    try {
                        ResUploadedFile result = request.execute().body();
                        if (result != null) {
                            UploadedFileInfo fileInfo = new UploadedFileInfo();
                            fileInfo.setMessageId(result.getMessageId());
                            fileInfo.setLocalPath(fileUploadDTO.getFilePath());
                            UploadedFileInfoRepository.getRepository().insertFileInfo(fileInfo);
                            fileUploadDTO.setUploadState(FileUploadDTO.UploadState.SUCCESS);
                        } else {
                            fileUploadDTO.setUploadState(FileUploadDTO.UploadState.FAIL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileUploadDTO.setUploadState(FileUploadDTO.UploadState.FAIL);
                    }
                    EventBus.getDefault().post(new FileUploadFinishEvent(fileUploadDTO));

                    boolean finishAll = isFinishAll();

                    if (!finishAll) {
                        notificationBuilder.setProgress(100, 100, false);
                        showNotification(context);
                    } else {
                        notificationBuilder.mActions.clear();
                        notificationBuilder
                                .setContentTitle(JandiApplication.getContext().getString(R.string.jandi_file_upload_finish))
                                .setContentText(JandiApplication.getContext().getString(R.string.jandi_file_upload_go_topic))
                                .setProgress(0, 0, false)
                                .setSmallIcon(R.drawable.icon_push_notification)
                                .setAutoCancel(true)
                                .setOngoing(false);
                        showNotification(context);
                    }

                    resetAllIfFinishedAll(finishAll);
                    increaseNotificationCount(finishAll);

                }, Throwable::printStackTrace);
    }

    private PendingIntent getPendingActivities(Context context, FileUploadDTO fileUploadDTO) {
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    1021,
                    UploadNotificationActivity.getIntent(context, fileUploadDTO.getTeamId(), fileUploadDTO.getEntity()),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    private void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void updateNotificationBuilder() {
        Intent intent = new Intent(JandiApplication.getContext(), UploadStopBroadcastReceiver.class);
        intent.putExtra(EXTRA_STOP, true);
        intent.putExtra(EXTRA_STOP_ID, notificationId);
        PendingIntent actionCancelIntent = PendingIntent.getBroadcast(JandiApplication.getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.mActions.clear();
        notificationBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, JandiApplication.getContext().getString(R.string.jandi_cancel), actionCancelIntent);
    }

    private void increaseNotificationCount(boolean finishAll) {
        if (finishAll) {
            ++notificationId;
        }
    }

    private void resetAllIfFinishedAll(boolean finishAll) {
        if (finishAll) {
            fileUploadDTOs.clear();
        }
    }

    private boolean isFinishAll() {
        for (FileUploadDTO fileUploadDTO : fileUploadDTOs) {
            if (fileUploadDTO.getUploadState() != FileUploadDTO.UploadState.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    public void add(FileUploadDTO fileUploadDTO) {
        fileUploadDTOs.add(fileUploadDTO);

        objectPublishSubject.onNext(fileUploadDTO);
    }

    public void remove(FileUploadDTO fileUploadDTO) {
        fileUploadDTOs.remove(fileUploadDTO);
    }

    public void retryAsFailed(long entityId) {
        FileUploadDTO temp;
        for (int idx = 0, size = fileUploadDTOs.size(); idx < size; idx++) {
            temp = fileUploadDTOs.get(idx);
            if (temp.getEntity() == entityId) {
                objectPublishSubject.onNext(temp);
                temp.setUploadState(FileUploadDTO.UploadState.IDLE);
            }
        }
    }

    public void retryAsFailed(FileUploadDTO item) {
        objectPublishSubject.onNext(item);
        item.setUploadState(FileUploadDTO.UploadState.IDLE);
    }

    public List<FileUploadDTO> getUploadInfos(long entityId) {

        List<FileUploadDTO> list = new ArrayList<>();
        Observable.from(fileUploadDTOs)
                .filter(fileUploadDTO -> fileUploadDTO.getEntity() == entityId)
                .filter(it -> it.getUploadState() != FileUploadDTO.UploadState.SUCCESS)
                .collect(() -> list, List::add)
                .subscribe();


        return list;
    }

    public void cancelAll() {

        if (subscription != null) {
            subscription.unsubscribe();
        }

        initSubscription(JandiApplication.getContext());

        Observable.from(fileUploadDTOs)
                .subscribe(fileUploadDTO -> {
                    if (fileUploadDTO.getUploadState() != FileUploadDTO.UploadState.SUCCESS) {
                        fileUploadDTO.setUploadState(FileUploadDTO.UploadState.SUCCESS);
                        EventBus.getDefault().post(new FileUploadFinishEvent(fileUploadDTO));
                    }
                }, t -> {
                });

        fileUploadDTOs.clear();
        if (lastRequest != null && lastRequest.isExecuted() && !lastRequest.isCanceled()) {
            lastRequest.cancel();
        }

    }
}
