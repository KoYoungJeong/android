package com.tosslab.jandi.app.services.upload;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadManager {

    private static FileUploadManager manager;
    private final PublishSubject<FileUploadDTO> objectPublishSubject;
    private List<FileUploadDTO> fileUploadDTOList;
    private Context context;
    private int notificationId = 100;

    private FileUploadManager(Context context) {
        this.context = context.getApplicationContext();
        fileUploadDTOList = new CopyOnWriteArrayList<>();

        objectPublishSubject = PublishSubject.create();
        objectPublishSubject.onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(fileUploadDTO -> {
                    int progress;
                    fileUploadDTO.setUploadState(FileUploadDTO.UploadState.PROGRESS);
                    EventBus.getDefault().post(new FileUploadStartEvent(fileUploadDTO.getEntity()));
                    FilePickerModel filePickerModel = FilePickerModel_.getInstance_(context);

                    boolean isPublicTopic = filePickerModel.isPublicEntity(fileUploadDTO.getEntity());

                    try {
                        JsonObject result = uploadFile(FileUploadManager.this.context,
                                fileUploadDTO.getFilePath(),
                                isPublicTopic, fileUploadDTO.getFileName(),
                                fileUploadDTO.getTeamId(),
                                fileUploadDTO.getEntity(),
                                fileUploadDTO.getComment(),
                                fileUploadDTO.getMentions(),
                                (downloaded, total) -> {
                                    FileUploadProgressEvent event = new FileUploadProgressEvent(fileUploadDTO.getEntity(), (int) (downloaded * 100 / total));
                                    EventBus.getDefault().post(event);
                                });

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
                            fileUploadDTOList.remove(fileUploadDTO);
                        } else {
                            fileUploadDTO.setUploadState(FileUploadDTO.UploadState.FAIL);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                        fileUploadDTO.setUploadState(FileUploadDTO.UploadState.FAIL);
                    }
                    EventBus.getDefault().post(new FileUploadFinishEvent(fileUploadDTO));

                    boolean finishAll = isFinishAll();
                    resetAllIfFinishedAll(finishAll);
                    decreaseNotificationCount(finishAll);

                }, Throwable::printStackTrace);
    }

    public static FileUploadManager getInstance(Context context) {
        if (manager == null) {
            manager = new FileUploadManager(context);
        }
        return manager;
    }

    private static JsonObject uploadFile(Context context,
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
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
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

        ResponseFuture<JsonObject> requestFuture = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        return requestFuture.get();

    }

    private void decreaseNotificationCount(boolean finishAll) {
        if (finishAll) {
            --notificationId;
        }
    }

    private void resetAllIfFinishedAll(boolean finishAll) {
        if (finishAll) {
            fileUploadDTOList.clear();
        }
    }

    private boolean isFinishAll() {
        boolean isFinishAll;
        for (FileUploadDTO fileUploadDTO : fileUploadDTOList) {
            if (fileUploadDTO.getUploadState() != FileUploadDTO.UploadState.SUCCESS) {
                isFinishAll = false;
                return true;
            }
        }
        return false;
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
}
