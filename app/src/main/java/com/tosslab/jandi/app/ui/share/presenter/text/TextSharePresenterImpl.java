package com.tosslab.jandi.app.ui.share.presenter.text;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
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
    private View view;
    ShareSelectModel shareSelectModel;

    @Override
    public void initViews() {
        if (!NetworkCheckUtil.isConnected()) {
            view.showFailToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
            view.finishOnUiThread();
            return;
        }
        EntityManager entityManager = EntityManager.getInstance();
        initEntityData(entityManager.getTeamId());
    }

    @Override
    @Background
    public void initEntityData(long teamId) {
        this.teamId = teamId;

        if (!shareModel.hasLeftSideMenu(teamId)) {
            try {
                ResLeftSideMenu leftSideMenu = shareModel.getLeftSideMenu(teamId);
                shareModel.updateLeftSideMenu(leftSideMenu);
            } catch (Exception e) {
                e.printStackTrace();
                view.moveIntro();
                return;
            }
        }

        shareSelectModel = shareModel.getShareSelectModel(teamId);

        String teamName = shareSelectModel.getTeamName();
        this.roomId = shareSelectModel.getDefaultTopicId();
        String roomName = shareSelectModel.getEntityById(roomId).getName();
        int roomType = JandiConstants.TYPE_PUBLIC_TOPIC;

        view.setTeamName(teamName);
        view.setRoomName(roomName);
        view.setMentionInfo(teamId, roomId, roomType);

    }

    private int getRoomType(FormattedEntity entity) {

        if (entity.isPrivateGroup()) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else if (entity.isPublicTopic()) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        }
    }

    @Override
    public void setEntity(long roomId) {
        this.roomId = roomId;
        FormattedEntity entity = shareSelectModel.getEntityById(roomId);
        int roomType = getRoomType(entity);

        view.setTeamName(shareSelectModel.getTeamName());
        view.setRoomName(entity.getName());
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
        int roomType = getRoomType(shareSelectModel.getEntityById(roomId));
        try {
            shareModel.sendMessage(teamId, roomId, roomType, messageText, mentions);
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
            return getEntityInfo();
        } else {
            try {
                EntityManager.getInstance();
                return true;
            } catch (Exception e) {
                return getEntityInfo();
            }
        }
    }

    private boolean getEntityInfo() {
        try {
            EntityClientManager entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance().refreshEntity();
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
