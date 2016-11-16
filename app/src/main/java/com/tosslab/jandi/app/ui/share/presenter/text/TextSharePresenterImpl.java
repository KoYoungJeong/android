package com.tosslab.jandi.app.ui.share.presenter.text;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;


@EBean
public class TextSharePresenterImpl implements TextSharePresenter {

    @Bean
    ShareModel shareModel;
    long roomId;
    long teamId;
    TeamInfoLoader teamInfoLoader;
    private View view;

    @Override
    public void initViews() {
        if (!NetworkCheckUtil.isConnected()) {
            view.showFailToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
            view.finishOnUiThread();
            return;
        }
        initEntityData(TeamInfoLoader.getInstance().getTeamId());
    }

    @Override
    @Background
    public void initEntityData(long teamId) {
        this.teamId = teamId;

        teamInfoLoader = shareModel.getTeamInfoLoader(teamId);
        shareModel.refreshPollList(teamId);

        String teamName = teamInfoLoader.getTeamName();
        this.roomId = teamInfoLoader.getDefaultTopicId();
        String roomName = teamInfoLoader.getName(roomId);
        int roomType = JandiConstants.TYPE_PUBLIC_TOPIC;

        view.setTeamName(teamName);
        view.setRoomName(roomName);
        view.setMentionInfo(teamId, roomId, roomType);

    }

    private int getRoomType(long entityId) {

        if (teamInfoLoader.isTopic(entityId)) {
            if (teamInfoLoader.isPublicTopic(entityId)) {
                return JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                return JandiConstants.TYPE_PRIVATE_TOPIC;
            }
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    @Override
    public void setEntity(long roomId) {
        this.roomId = roomId;
        int roomType = getRoomType(roomId);

        view.setTeamName(teamInfoLoader.getTeamName());
        view.setRoomName(teamInfoLoader.getName(roomId));
        view.setMentionInfo(teamId, roomId, roomType);


    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    @Override
    @Background
    public void sendMessage(String messageText, List<MentionObject> mentions) {
        view.showProgressBar();
        int roomType = getRoomType(roomId);
        try {
            shareModel.sendMessage(teamId, roomId, messageText, mentions);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_share_succeed, messageText));
            view.finishOnUiThread();
        } catch (RetrofitException e) {
            e.printStackTrace();
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
        } finally {
            view.dismissProgressBar();
            setupSelectedTeam(teamId);
            view.moveEntity(teamId, roomId, roomType);
        }
    }

    private boolean setupSelectedTeam(long teamId) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        }
        return true;
    }
}
