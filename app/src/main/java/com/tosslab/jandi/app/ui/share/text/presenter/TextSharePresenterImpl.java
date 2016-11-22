package com.tosslab.jandi.app.ui.share.text.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;

import javax.inject.Inject;

import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TextSharePresenterImpl implements TextSharePresenter {

    ShareModel shareModel;
    View view;

    long roomId = -1;
    long teamId;
    long entityId = -1;
    TeamInfoLoader teamInfoLoader;

    @Inject
    public TextSharePresenterImpl(ShareModel shareModel, View view) {
        this.shareModel = shareModel;
        this.view = view;
    }

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
    public void initEntityData(long teamId) {
        this.teamId = teamId;

        Completable.fromAction(() -> {
            teamInfoLoader = shareModel.getTeamInfoLoader(teamId);
            shareModel.refreshPollList(teamId);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
            String teamName = teamInfoLoader.getTeamName();
            view.setTeamName(teamName);
        }, Throwable::printStackTrace);


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
    public void setEntity(long roomId, int roomType) {
        if (roomType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            this.roomId = TeamInfoLoader.getInstance().getChatId(roomId);
            this.entityId = roomId;
        } else {
            this.roomId = roomId;
            this.entityId = roomId;
        }
        Completable.fromAction(() -> {

            view.setTeamName(teamInfoLoader.getTeamName());
            view.setRoomName(teamInfoLoader.getName(roomId));
            view.setMentionInfo(teamId, roomId, roomType);
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    @Override
    public void sendMessage(String messageText, List<MentionObject> mentions) {

        if (teamId <= 0 || roomId <= 0) {
            view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_title_cdp_to_be_shared));
            return;
        }

        view.showProgressBar();
        int roomType = getRoomType(roomId);
        Completable.fromCallable(() -> {
            shareModel.sendMessage(teamId, roomId, messageText, mentions);
            return true;
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgressBar();
                    view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_share_succeed, messageText));
                    setupSelectedTeam(teamId);
                    view.moveEntity(teamId, roomId, entityId, roomType);
                    view.finishOnUiThread();
                }, t -> {
                    t.printStackTrace();
                    view.dismissProgressBar();
                    view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
                });
    }

    private boolean setupSelectedTeam(long teamId) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        }
        return true;
    }
}
