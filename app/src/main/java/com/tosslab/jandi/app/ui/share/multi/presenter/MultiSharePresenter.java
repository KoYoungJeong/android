package com.tosslab.jandi.app.ui.share.multi.presenter;

import android.util.Pair;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

public interface MultiSharePresenter {

    void onRoomChange();

    void initShareTarget();

    void onSelectTeam(long teamId);

    void initShareData(List<String> uris);

    void onSelectRoom(long roomId);

    void startShare(List<Pair<String, List<MentionObject>>> mentionInfos);

    interface View {

        void callRoomSelector(long teamId);

        void updateFiles();

        void moveIntro();

        void setTeamName(String teamName);

        void setRoomName(String roomName);

        void setMentionInfo(long teamId, long roomId);


        void setFileTitle(String fileName);
    }
}
