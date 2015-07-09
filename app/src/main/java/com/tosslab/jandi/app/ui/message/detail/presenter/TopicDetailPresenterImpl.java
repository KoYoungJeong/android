package com.tosslab.jandi.app.ui.message.detail.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.TopicDetailModel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 7. 9..
 */
@EBean
public class TopicDetailPresenterImpl implements TopicDetailPresenter {

    @Bean
    TopicDetailModel topicDetailModel;

    @Bean
    InvitationViewModel invitationViewModel;

    @Bean
    EntityClientManager entityClientManager;

    private View view;

    @Override
    public void setView(View view) {

        this.view = view;
    }

    @Override
    public void onInit(Context context, int entityId) {
        String topicName = topicDetailModel.getTopicName(context, entityId);
        String topicDescription = topicDetailModel.getTopicDescription(context, entityId);
        int topicMemberCount = topicDetailModel.getTopicMemberCount(context, entityId);
        boolean isStarred = topicDetailModel.isStarred(context, entityId);

        view.setTopicName(topicName);
        view.setTopicDescription(topicDescription);
        view.setStarred(isStarred);
        view.setTopicMemberCount(topicMemberCount);
    }

    @Override
    public void onTopicInvite(Context context, int entityId) {
        invitationViewModel.initData(context, entityId);
        invitationViewModel.invite();
    }

    @Override
    public void onTopicDescriptionMove(Context context, int entityId) {
        if (invitationViewModel.isTopicOwner(context, entityId)) {
            // TODO 토픽 소개 수정 페이지 이동
        }
    }

    @Background
    @Override
    public void onTopicStar(Context context, int entityId) {
        boolean isStarred = topicDetailModel.isStarred(context, entityId);

        try {

            if (isStarred) {
                entityClientManager.disableFavorite(entityId);
                view.showSuccessToast(context.getString(R.string.jandi_message_starred));
            } else {
                entityClientManager.enableFavorite(entityId);
            }

            EntityManager.getInstance(context).getEntityById(entityId).isStarred = !isStarred;

            view.setStarred(!isStarred);

        } catch (RetrofitError e) {

        }
    }
}
