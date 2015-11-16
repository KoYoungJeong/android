package com.tosslab.jandi.app.ui.file.upload.preview.presenter;

import android.app.Activity;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;

import java.util.ArrayList;
import java.util.List;

public interface FileUploadPresenter {

    void setView(View view);

    void onInitViewPager(int selectedEntityIdToBeShared, ArrayList<String> realFilePathList);

    void onPagerSelect(int position);

    void onInitEntity(Activity activity, int selectedEntityIdToBeShared);

    void onCommentTextChange(String text, int currentItemPosition);

    void onEntityUpdate(FormattedEntity item);

    @Deprecated
    void onSingleFileUpload();

    void onMultiFileUpload(MentionControlViewModel mentionControlViewModel);

    interface View {

        void initViewPager(List<String> realFilePathList);

        void setFileName(String fileName);

        void setComment(String comment);

        void setEntityInfo(String entity);

        void showEntitySelectDialog(List<FormattedEntity> entityList);

        void exitOnOK();

        void exitOnOk(FileUploadVO fileUploadVO);

        void setShareEntity(int entityId, boolean isUser);
    }
}
