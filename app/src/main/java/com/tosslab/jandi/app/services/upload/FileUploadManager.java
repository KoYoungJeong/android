package com.tosslab.jandi.app.services.upload;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.FileUploadProgressEvent;
import com.tosslab.jandi.app.events.files.FileUploadStartEvent;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel_;
import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
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
    private ResponseFuture<JsonObject> lastRequest;
    private List<FileUploadDTO> fileUploadDTOList;
    private int notificationId = 100;
    private NotificationCompat.Builder notificationBuilder;
    private Subscription subscription;
    private PendingIntent pendingIntent;

    private FileUploadManager() {
        Context context = JandiApplication.getContext();
        fileUploadDTOList = new CopyOnWriteArrayList<>();

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

    private static ResponseFuture<JsonObject> uploadFile(Context context,
                                                         String realFilePath,
                                                         boolean isPublicTopic,
                                                         String title, long teamId, long entityId,
                                                         String comment, List<MentionObject> mentions,
                                                         ProgressCallback progressCallback) throws ExecutionException, InterruptedException {
        File uploadFile = new File(realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_FILE_UPLOAD_URL + "inner-api/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgress(progressCallback)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent())
                .setMultipartParameter("title", title)
                .setMultipartParameter("share", String.valueOf(entityId))
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(teamId));

        // Comment가 함께 등록될 경우 추가
        if (comment != null && !comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", comment);
            try {
                ionBuilder.setMultipartParameter("mentions", JacksonMapper.getInstance().getObjectMapper().writeValueAsString(mentions));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

    }

    private void initSubscription(Context context) {
        subscription = objectPublishSubject.onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(fileUploadDTO -> {

                    notificationBuilder.setProgress(100, 0, false)
                            .setAutoCancel(false)
                            .setContentTitle(JandiApplication.getContext().getString(R.string.jandi_file_upload_state, (fileUploadDTOList.indexOf(fileUploadDTO) + 1), fileUploadDTOList.size()))
                            .setContentIntent(getPendingActivities(context, fileUploadDTO))
                            .setSmallIcon(android.R.drawable.stat_sys_upload);
                    updateNotificationBuilder();
                    showNotification(context);

                    final int[] progress = {0, 0};
                    fileUploadDTO.setUploadState(FileUploadDTO.UploadState.PROGRESS);
                    EventBus.getDefault().post(new FileUploadStartEvent(fileUploadDTO.getEntity()));
                    FilePickerModel filePickerModel = FilePickerModel_.getInstance_(context);

                    boolean isPublicTopic = filePickerModel.isPublicEntity(fileUploadDTO.getEntity());

                    try {
                        ResponseFuture<JsonObject> uploadFuture = uploadFile(JandiApplication.getContext(),
                                fileUploadDTO.getFilePath(),
                                isPublicTopic, fileUploadDTO.getFileName(),
                                fileUploadDTO.getTeamId(),
                                fileUploadDTO.getEntity(),
                                fileUploadDTO.getComment(),
                                fileUploadDTO.getMentions(),
                                (downloaded, total) -> {
                                    progress[1] = (int) (downloaded * 100 / total);
                                    FileUploadProgressEvent event = new FileUploadProgressEvent(fileUploadDTO.getEntity(), (int) (downloaded * 100 / total));
                                    EventBus.getDefault().post(event);
                                    if (progress[0] != progress[1]) {
                                        progress[0] = progress[1];
                                        notificationBuilder.setContentTitle(JandiApplication.getContext().getString(R.string.jandi_file_upload_state, (fileUploadDTOList.indexOf(fileUploadDTO) + 1), fileUploadDTOList.size()))
                                                .setContentText(JandiApplication.getContext().getString(R.string.app_name));
                                        notificationBuilder.setProgress(100, progress[0], false);
                                        showNotification(context);
                                    }
                                });
                        lastRequest = uploadFuture;
                        JsonObject result = uploadFuture.get();

                        if (result.get("code") == null) {
                            try {
                                ResUploadedFile resUploadedFile = JacksonMapper.getInstance().getObjectMapper().readValue(result
                                        .toString(), ResUploadedFile.class);
                                UploadedFileInfo fileInfo = new UploadedFileInfo();
                                fileInfo.setMessageId(resUploadedFile.getMessageId());
                                fileInfo.setLocalPath(fileUploadDTO.getFilePath());
                                UploadedFileInfoRepository.getRepository().insertFileInfo(fileInfo);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            fileUploadDTO.setUploadState(FileUploadDTO.UploadState.SUCCESS);
                        } else {
                            fileUploadDTO.setUploadState(FileUploadDTO.UploadState.FAIL);
                        }
                    } catch (CancellationException | InterruptedException e) {
                        // do nothing.
                    } catch (Exception e) {
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
            fileUploadDTOList.clear();
        }
    }

    private boolean isFinishAll() {
        for (FileUploadDTO fileUploadDTO : fileUploadDTOList) {
            if (fileUploadDTO.getUploadState() != FileUploadDTO.UploadState.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    public void add(FileUploadDTO fileUploadDTO) {
        fileUploadDTOList.add(fileUploadDTO);

        objectPublishSubject.onNext(fileUploadDTO);
    }

    public void remove(FileUploadDTO fileUploadDTO) {
        fileUploadDTOList.remove(fileUploadDTO);
    }

    public void retryAsFailed(long entityId) {
        FileUploadDTO temp;
        for (int idx = 0, size = fileUploadDTOList.size(); idx < size; idx++) {
            temp = fileUploadDTOList.get(idx);
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
        Observable.from(fileUploadDTOList)
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

        Observable.from(fileUploadDTOList)
                .subscribe(fileUploadDTO -> {
                    if (fileUploadDTO.getUploadState() != FileUploadDTO.UploadState.SUCCESS) {
                        fileUploadDTO.setUploadState(FileUploadDTO.UploadState.SUCCESS);
                        EventBus.getDefault().post(new FileUploadFinishEvent(fileUploadDTO));
                    }
                }, t -> {});

        fileUploadDTOList.clear();
        if (lastRequest != null && !lastRequest.isDone() && !lastRequest.isCancelled()) {
            lastRequest.cancel(true);
        }

    }
}
