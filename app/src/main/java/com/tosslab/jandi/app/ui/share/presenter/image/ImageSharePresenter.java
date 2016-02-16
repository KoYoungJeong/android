package com.tosslab.jandi.app.ui.share.presenter.image;

import android.app.ProgressDialog;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.io.File;
import java.util.List;

public interface ImageSharePresenter {
    void setView(ImageSharePresenterImpl.View view);

    void initView(String uriString);

    void initEntityData(long teamId, String teamName);

    void setEntityData(long roomId, String roomName, int roomType);

    void uploadFile(File imageFile,
                    String tvTitle, String commentText,
                    ProgressDialog uploadProgress, List<MentionObject> mentions);

    File getImageFile();

    long getTeamId();

    interface View {
        void showProgressBar();

        void dismissProgressBar();

        void bindImage(File imagePath);

        void finishOnUiThread();

        void showSuccessToast(String message);

        void showFailToast(String message);

        void setTeamName(String name);

        void setRoomName(String name);

        void moveEntity(long teamId, long entityId, int entityType);

        String getComment();

        void setComment(String comment);

        void setMentionInfo(long teamId, long roomId, int roomType);

        void dismissDialog(ProgressDialog uploadProgress);

        void moveIntro();
    }
}