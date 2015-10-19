package com.tosslab.jandi.app.services.upload;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.FileUploadProgressEvent;
import com.tosslab.jandi.app.events.files.FileUploadStartEvent;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel_;
import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;
import com.tosslab.jandi.app.local.orm.repositories.UploadedFileInfoRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func0;
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

    private FileUploadManager(Context context) {
        this.context = context.getApplicationContext();
        fileUploadDTOList = new CopyOnWriteArrayList<FileUploadDTO>();

        objectPublishSubject = PublishSubject.create();
        objectPublishSubject.onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(fileUploadDTO -> {
                    fileUploadDTO.setUploadState(FileUploadDTO.UploadState.PROGRESS);
                    EventBus.getDefault().post(new FileUploadStartEvent(fileUploadDTO.getEntity()));
                    FilePickerModel filePickerModel = FilePickerModel_.getInstance_(context);

                    boolean isPublicTopic = filePickerModel.isPublicEntity(context, fileUploadDTO.getEntity());

                    try {
                        JsonObject result = filePickerModel.uploadFile(FileUploadManager.this.context, fileUploadDTO.getFilePath(), isPublicTopic, fileUploadDTO.getFileName(), fileUploadDTO.getEntity(), fileUploadDTO.getComment(), new ProgressCallback() {
                            @Override
                            public void onProgress(long downloaded, long total) {
                                EventBus.getDefault().post(new FileUploadProgressEvent(fileUploadDTO.getEntity(), (int) (downloaded * 100 / total)));
                            }
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

                }, Throwable::printStackTrace);
    }

    public static FileUploadManager getInstance(Context context) {
        if (manager == null) {
            manager = new FileUploadManager(context);
        }
        return manager;
    }

    public void add(FileUploadDTO fileUploadDTO) {
        fileUploadDTOList.add(fileUploadDTO);
        objectPublishSubject.onNext(fileUploadDTO);
    }

    public void remove(FileUploadDTO fileUploadDTO) {
        fileUploadDTOList.remove(fileUploadDTO);
    }

    public void retryAsFailed(int entityId) {
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

    public List<FileUploadDTO> getUploadInfos(int entityId) {

        List<FileUploadDTO> list = new ArrayList<FileUploadDTO>();
        Observable.from(fileUploadDTOList)
                .filter(fileUploadDTO -> fileUploadDTO.getEntity() == entityId)
                .collect(new Func0<List<FileUploadDTO>>() {
                    @Override
                    public List<FileUploadDTO> call() {
                        return list;
                    }
                }, new Action2<List<FileUploadDTO>, FileUploadDTO>() {
                    @Override
                    public void call(List<FileUploadDTO> fileUploadDTOList, FileUploadDTO fileUploadDTO1) {
                        fileUploadDTOList.add(fileUploadDTO1);
                    }
                })
                .subscribe();


        return list;
    }
}
