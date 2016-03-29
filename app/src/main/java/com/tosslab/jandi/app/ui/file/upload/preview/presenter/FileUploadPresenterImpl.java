package com.tosslab.jandi.app.ui.file.upload.preview.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.file.upload.preview.model.FileUploadModel;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EBean
public class FileUploadPresenterImpl implements FileUploadPresenter {

    @Bean
    FileUploadModel fileUploadModel;
    private View view;

    private List<FileUploadVO> fileUploadVOs;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInitEntity(Activity activity, long selectedEntityIdToBeShared) {

        String entityName;
        long entityId;
        if (fileUploadModel.isValid(selectedEntityIdToBeShared)) {
            entityName = fileUploadModel.getEntityString(selectedEntityIdToBeShared);
            entityId = selectedEntityIdToBeShared;
        } else {
            FormattedEntity entity = fileUploadModel.getDefaultTopicEntity();
            entityId = entity.getId();
            entityName = entity.getName();
        }

        FormattedEntity entity = fileUploadModel.getEntity(entityId);

        view.setEntityInfo(entityName);
        view.setShareEntity(entityId, entity.isUser());

    }

    @Override
    public void onCommentTextChange(String text, int currentItemPosition) {
        fileUploadVOs.get(currentItemPosition).setComment(text);
    }

    @Override
    public void onEntityUpdate(FormattedEntity item) {
        long id = item.getId();
        for (FileUploadVO fileUploadVO : fileUploadVOs) {
            fileUploadVO.setEntity(id);
        }

        view.setEntityInfo(fileUploadModel.getEntityString(id));
    }

    @Deprecated
    @Override
    public void onSingleFileUpload() {
        if (fileUploadVOs == null || fileUploadVOs.isEmpty()) {
            return;
        }

        FileUploadVO fileUploadVO = fileUploadVOs.get(0);
        view.exitOnOk(fileUploadVO);
    }

    @Override
    public void onMultiFileUpload(MentionControlViewModel mentionControlViewModel) {
        FileUploadManager instance = FileUploadManager.getInstance();

        for (FileUploadVO fileUploadVO : fileUploadVOs) {
            FileUploadDTO fileUploadDTO;
            if (mentionControlViewModel != null) {

                ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject(fileUploadVO.getComment());

                fileUploadDTO = new FileUploadDTO();
                fileUploadDTO.setTeamId(EntityManager.getInstance().getTeamId());
                fileUploadDTO.setMentions(mentionInfoObject.getMentions());
                fileUploadDTO.setComment(mentionInfoObject.getMessage());
                fileUploadDTO.setFilePath(fileUploadVO.getFilePath());
                fileUploadDTO.setFileName(fileUploadVO.getFileName());
                fileUploadDTO.setEntity(fileUploadVO.getEntity());
            } else {
                fileUploadDTO = new FileUploadDTO(fileUploadVO);
                fileUploadDTO.setTeamId(EntityManager.getInstance().getTeamId());
                fileUploadDTO.setMentions(new ArrayList<>());
            }

            instance.add(fileUploadDTO);
        }

        view.exitOnOK();
    }

    @Override
    public void onInitViewPager(long selectedEntityIdToBeShared, ArrayList<String> realFilePathList) {
        fileUploadVOs = new ArrayList<FileUploadVO>();

        for (String filePath : realFilePathList) {
            fileUploadVOs.add(new FileUploadVO.Builder()
                    .entity(selectedEntityIdToBeShared)
                    .filePath(filePath)
                    .fileName(fileUploadModel.getFileName(filePath))
                    .createFileUploadInfo());
        }

        view.initViewPager(Collections.unmodifiableList(realFilePathList));
    }

    @Override
    public void onPagerSelect(int position) {
        FileUploadVO fileUploadVO = fileUploadVOs.get(position);

        view.setFileName(fileUploadVO.getFileName());
        view.setComment(fileUploadVO.getComment());
        view.setEntityInfo(fileUploadModel.getEntityString(fileUploadVO.getEntity()));
    }
}
