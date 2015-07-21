package com.tosslab.jandi.app.ui.file.upload.preview.presenter;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.ui.file.upload.preview.model.FileUploadModel;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EBean
public class FileUploadPresenterImpl implements FileUploadPresenter {

    @RootContext
    Context context;

    @Bean
    FileUploadModel fileUploadModel;
    private View view;

    private List<FileUploadVO> fileUploadVOs;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInitEntity(int selectedEntityIdToBeShared) {

        String entityName;
        if (fileUploadModel.isValid(context, selectedEntityIdToBeShared)) {
            entityName = fileUploadModel.getEntityString(context, selectedEntityIdToBeShared);
        } else {
            FormattedEntity entity = fileUploadModel.getEntityInfoWithoutMe(context).get(0);
            int id = entity.getId();
            view.setShareEntity(id);
            entityName = entity.getName();
        }

        view.setEntityInfo(entityName);
    }

    @Override
    public void onEntitySelect(int currentItem) {

        int entity = fileUploadVOs.get(currentItem).getEntity();

        List<FormattedEntity> entityList = fileUploadModel.getEntityInfoWithoutMe(context);

        view.showEntitySelectDialog(entityList);

    }

    @Override
    public void onCommentTextChange(String text, int currentItemPosition) {
        fileUploadVOs.get(currentItemPosition).setComment(text);
    }

    @Override
    public void onEntityUpdate(FormattedEntity item) {
        int id = item.getId();
        for (FileUploadVO fileUploadVO : fileUploadVOs) {
            fileUploadVO.setEntity(id);
        }

        view.setEntityInfo(fileUploadModel.getEntityString(context, id));
    }

    @Override
    public void onUploadStartFile() {
        FileUploadManager instance = FileUploadManager.getInstance(context);

        for (FileUploadVO fileUploadVO : fileUploadVOs) {
            instance.add(new FileUploadDTO(fileUploadVO));
        }

        view.exitOnOK();
    }

    @Override
    public void onInitViewPager(int selectedEntityIdToBeShared, ArrayList<String> realFilePathList) {
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
        view.setEntityInfo(fileUploadModel.getEntityString(context, fileUploadVO.getEntity()));
    }
}
