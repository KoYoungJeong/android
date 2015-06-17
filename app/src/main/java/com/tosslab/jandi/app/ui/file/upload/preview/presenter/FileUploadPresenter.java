package com.tosslab.jandi.app.ui.file.upload.preview.presenter;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.ArrayList;
import java.util.List;

public interface FileUploadPresenter {

    void setView(View view);

    void onInitViewPager(int selectedEntityIdToBeShared, ArrayList<String> realFilePathList);

    void onPagerSelect(int position);

    void onInitEntity(int selectedEntityIdToBeShared);

    void onEntitySelect(int currentItem);

    void onCommentTextChange(String text, int currentItemPosition);

    void onEntityUpdate(FormattedEntity item);

    void onUploadStartFile();

    interface View {

        void initViewPager(List<String> realFilePathList);

        void setFileName(String fileName);

        void setComment(String comment);

        void setEntityInfo(String entity);

        void showEntitySelectDialog(List<FormattedEntity> entityList);

        void exitOnOK();

    }
}
