package com.tosslab.jandi.app.ui.file.upload.preview.presenter;

import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;

import java.util.List;

public interface FileUploadPresenter {

    void setView(View view);

    void onPagerSelect(int position);

    void onInitEntity(long selectedEntityIdToBeShared, List<String> realFilePathList);

    void onCommentTextChange(String text, int currentItemPosition);

    void onInitPricingInfo();

    void onMultiFileUpload(MentionControlViewModel mentionControlViewModel);

    String getFileName(int currentItem);

    void changeFileName(int position, String fileName);

    interface View {

        void initViewPager(List<String> realFilePathList);

        void setFileName(String fileName);

        void setComment(String comment);

        void setEntityInfo(String entity);

        void exitOnOK();

        void exitOnOk(FileUploadVO fileUploadVO);

        void setShareEntity(long entityId, boolean isUser);

        void setPricingLimitView(Boolean isLimited);
    }
}
