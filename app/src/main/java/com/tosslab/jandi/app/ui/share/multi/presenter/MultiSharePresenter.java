package com.tosslab.jandi.app.ui.share.multi.presenter;

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

    interface View {

        void setUpScrollButton(int position, int count);

        void callRoomSelector(long teamId);

        void updateFiles(int pageCount);

        void moveIntro();

        void setTeamName(String teamName);

        void setRoomName(String roomName);

        void setMentionInfo(long teamId, long roomId);

        void setCommentText(String comment);

        void setFileTitle(String fileName);

        void moveRoom(long teamId, long roomId);

        void showProgress();

        void dismissProgress();

        void showSelectRoomToast();
    }
}
