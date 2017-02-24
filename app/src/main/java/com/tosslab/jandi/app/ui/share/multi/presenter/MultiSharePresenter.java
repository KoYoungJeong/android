package com.tosslab.jandi.app.ui.share.multi.presenter;

import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbAdapter;

import java.util.List;

public interface MultiSharePresenter {

    void onRoomChange();

    void initShareTarget();

    void onSelectTeam(long teamId);

    void initShareData(List<String> uris);

    void onSelectRoom(long roomId);

    void startShare();

    void onFilePageChanged(int position, String comment);

    void updateComment(int currentItem, String comment);

    void changeFileName(int position, String fileName);

    String getFileName(int position);

    interface View {

        void setUpScrollButton(int position, int count);

        void callRoomSelector(long teamId);

        void updateFiles(int pageCount);

        void moveIntro();

        void setTeamName(String teamName);

        void setTeamDefaultName();

        void setRoomName(String roomName);

        void setMentionInfo(long teamId, long roomId);

        void setCommentText(String comment);

        void moveRoom(long teamId, long roomId);

        void showProgress();

        void dismissProgress();

        void showSelectRoomToast();

        void setFileName(String fileName);

        void setFileThumbInfos(List<FileUploadThumbAdapter.FileThumbInfo> fileThumbInfos);
    }
}
