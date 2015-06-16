package com.tosslab.jandi.app.ui.file.upload.presenter;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.file.upload.model.FileUploadModel;
import com.tosslab.jandi.app.ui.file.upload.to.FileUploadInfo;

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

    private List<FileUploadInfo> fileUploadInfos;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onInitEntity(int selectedEntityIdToBeShared) {

        String entityName = fileUploadModel.getEntityString(context, selectedEntityIdToBeShared);
        view.setEntityInfo(entityName);
    }

    @Override
    public void onEntitySelect(int currentItem) {

        int entity = fileUploadInfos.get(currentItem).getEntity();

        List<FormattedEntity> entityList = fileUploadModel.getEntityInfoWithoutMe(context);

        view.showEntitySelectDialog(entityList);

    }

    @Override
    public void onCommentTextChange(String text, int currentItemPosition) {
        fileUploadInfos.get(currentItemPosition).setComment(text);
    }

    @Override
    public void onEntityUpdate(FormattedEntity item) {
        int id = item.getId();
        for (FileUploadInfo fileUploadInfo : fileUploadInfos) {
            fileUploadInfo.setEntity(id);
        }

        view.setEntityInfo(fileUploadModel.getEntityString(context, id));
    }

    @Override
    public void onInitViewPager(int selectedEntityIdToBeShared, ArrayList<String> realFilePathList) {
        fileUploadInfos = new ArrayList<FileUploadInfo>();

        for (String filePath : realFilePathList) {
            fileUploadInfos.add(new FileUploadInfo.Builder()
                    .entity(selectedEntityIdToBeShared)
                    .filePath(filePath)
                    .fileName(fileUploadModel.getFileName(filePath))
                    .createFileUploadInfo());
        }

        view.initViewPager(Collections.unmodifiableList(realFilePathList));
    }

    @Override
    public void onPagerSelect(int position) {
        FileUploadInfo fileUploadInfo = fileUploadInfos.get(position);

        view.setFileName(fileUploadInfo.getFileName());
        view.setComment(fileUploadInfo.getComment());
        view.setEntityInfo(fileUploadModel.getEntityString(context, fileUploadInfo.getEntity()));
    }
}
