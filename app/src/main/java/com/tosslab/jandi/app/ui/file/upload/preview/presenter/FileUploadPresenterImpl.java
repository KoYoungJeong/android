package com.tosslab.jandi.app.ui.file.upload.preview.presenter;

import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.model.FileUploadModel;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;
import com.tosslab.jandi.app.utils.file.FileUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class FileUploadPresenterImpl implements FileUploadPresenter {

    FileUploadModel fileUploadModel;
    private View view;

    private List<FileUploadVO> fileUploadVOs;

    @Inject
    public FileUploadPresenterImpl(View view, FileUploadModel fileUploadModel) {
        this.fileUploadModel = fileUploadModel;
        this.view = view;
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInitEntity(long entityId, List<String> realFilePathList) {

        String entityName;
        if (fileUploadModel.isValid(entityId)) {
            entityName = fileUploadModel.getEntityString(entityId);
        } else {
            long topicId = fileUploadModel.getDefaultTopicEntity();
            entityId = topicId;
            entityName = fileUploadModel.getEntityName(topicId);
        }

        if (entityId == -1) {
            view.exitOnFail();
            return;
        }

        view.setEntityInfo(entityName);
        view.setShareEntity(entityId, fileUploadModel.isUser(entityId));

        Observable.just(entityId)
                .map(it -> {
                    long roomId = -1;
                    if (TeamInfoLoader.getInstance().isUser(it)) {
                        long chatId = TeamInfoLoader.getInstance().getChatId(it);
                        if (chatId > 0) {
                            roomId = chatId;
                        } else {
                            try {
                                roomId = new ChatApi(InnerApiRetrofitBuilder.getInstance()).createChat(TeamInfoLoader.getInstance().getTeamId(), it).getId();
                            } catch (RetrofitException e) {
                                e.printStackTrace();
                                roomId = TeamInfoLoader.getInstance().getDefaultTopicId();
                            }
                        }
                    } else {
                        roomId = it;
                    }
                    return roomId;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(roomId -> {
                    onInitViewPager(roomId, realFilePathList);
                }, Throwable::printStackTrace);


    }

//    @Override
//    public void onInitPricingInfo() {
//        Observable.defer(() -> {
//            boolean isLimited = fileUploadModel.isUploadLimited();
//            return Observable.just(isLimited);
//        }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(isLimited -> {
//                    view.setPricingLimitView(isLimited);
//                });
//    }

    @Override
    public void onCommentTextChange(String text, int currentItemPosition) {
        fileUploadVOs.get(currentItemPosition).setComment(text);
    }

    @Override
    public void onMultiFileUpload(MentionControlViewModel mentionControlViewModel) {
        FileUploadManager instance = FileUploadManager.getInstance();

        for (FileUploadVO fileUploadVO : fileUploadVOs) {
            FileUploadDTO fileUploadDTO;
            if (mentionControlViewModel != null) {

                ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject(fileUploadVO.getComment());

                fileUploadDTO = new FileUploadDTO();
                fileUploadDTO.setTeamId(TeamInfoLoader.getInstance().getTeamId());
                fileUploadDTO.setMentions(mentionInfoObject.getMentions());
                fileUploadDTO.setComment(mentionInfoObject.getMessage());
                fileUploadDTO.setFilePath(fileUploadVO.getFilePath());
                fileUploadDTO.setFileName(fileUploadVO.getFileName());
                fileUploadDTO.setEntity(fileUploadVO.getEntity());
            } else {
                fileUploadDTO = new FileUploadDTO(fileUploadVO);
                fileUploadDTO.setTeamId(TeamInfoLoader.getInstance().getTeamId());
                fileUploadDTO.setMentions(new ArrayList<>());
            }

            instance.add(fileUploadDTO);
        }

        view.exitOnOK();
    }

    public void onInitViewPager(long selectedEntityIdToBeShared, List<String> realFilePathList) {
        fileUploadVOs = new ArrayList<>();

        for (String filePath : realFilePathList) {
            fileUploadVOs.add(new FileUploadVO.Builder()
                    .entity(selectedEntityIdToBeShared)
                    .filePath(filePath)
                    .fileName(FileUtil.convertAvailableFileName(fileUploadModel.getFileName(filePath)))
                    .createFileUploadInfo());
        }

        view.initViewPager(Collections.unmodifiableList(realFilePathList));
    }

    @Override
    public void onPagerSelect(int position) {
        FileUploadVO fileUploadVO = fileUploadVOs.get(position);

        view.setFileName(fileUploadVO.getFileName());
        view.setComment(fileUploadVO.getComment());
    }

    @Override
    public String getFileName(int position) {
        return fileUploadVOs.get(position).getFileName();
    }

    @Override
    public void changeFileName(int position, String fileName) {
        fileUploadVOs.get(position).setFileName(fileName);
    }

    @Override
    public void initThumbInfo(List<String> realFilePathList) {
        Observable.from(realFilePathList)
                .map(FileUploadThumbAdapter.FileThumbInfo::create)
                .collect((Func0<ArrayList<FileUploadThumbAdapter.FileThumbInfo>>) ArrayList::new, List::add)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileThumbInfos -> {
                    fileThumbInfos.get(0).setSelected(true);
                    view.setFileThumbInfo(fileThumbInfos);
                });
    }

}