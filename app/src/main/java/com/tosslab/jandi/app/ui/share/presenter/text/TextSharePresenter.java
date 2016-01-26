package com.tosslab.jandi.app.ui.share.presenter.text;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

public interface TextSharePresenter {
    void initViews();

    void initEntityData(int teamId);

    void setEntity(int roomId);

    void setView(View view);

    int getTeamId();

    void sendMessage(String messageText, List<MentionObject> mentions);

    interface View {

        void showFailToast(String message);

        void finishOnUiThread();

        void moveIntro();

        void setTeamName(String teamName);

        void setRoomName(String roomName);

        void setMentionInfo(int teamId, int roomId, int roomType);

        void showProgressBar();

        void showSuccessToast(String message);

        void dismissProgressBar();

        void moveEntity(int teamId, int roomId, int roomType);
    }
}
