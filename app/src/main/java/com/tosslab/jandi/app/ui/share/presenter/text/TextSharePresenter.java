package com.tosslab.jandi.app.ui.share.presenter.text;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

public interface TextSharePresenter {
    void initViews();

    void initEntityData(long teamId);

    void setEntity(long roomId);

    void setView(View view);

    long getTeamId();

    void sendMessage(String messageText, List<MentionObject> mentions);

    interface View {

        void showFailToast(String message);

        void finishOnUiThread();

        void moveIntro();

        void setTeamName(String teamName);

        void setRoomName(String roomName);

        void setMentionInfo(long teamId, long roomId, int roomType);

        void showProgressBar();

        void showSuccessToast(String message);

        void dismissProgressBar();

        void moveEntity(long teamId, long roomId, int roomType);
    }
}
