package com.tosslab.jandi.app.ui.maintab.topic.views.create.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.maintab.topic.views.create.model.TopicCreateModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;


import retrofit.client.Response;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class TopicCreatePresenterImpl implements TopicCreatePresenter {

    @Bean
    TopicCreateModel topicCreateModel;
    private View view;

    @Override
    @Background
    public void onCreateTopic(String topicTitle, String topicDescriptionText, boolean isPublic, boolean isAutojoin) {

        if (!NetworkCheckUtil.isConnected()) {
            view.showCheckNetworkDialog();
            return;
        }

        if (topicCreateModel.invalideTitle(topicTitle)) {
            return;
        }

        view.showProgressWheel();
        try {
            ResCommon topic = topicCreateModel.createTopic(topicTitle, isPublic, topicDescriptionText, isAutojoin);

            try {
                EntityManager mEntityManager = EntityManager.getInstance();
                if (mEntityManager != null) {
                    MixpanelMemberAnalyticsClient
                            .getInstance(JandiApplication.getContext(), mEntityManager.getDistictId())
                            .trackCreatingEntity(true);
                }
            } catch (JSONException e) {
                LogUtil.e("CAN NOT MEET", e);
            }

            topicCreateModel.refreshEntity();

            view.dismissProgressWheel();

            EntityManager.getInstance().refreshEntity();
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            topicCreateModel.trackTopicCreateSuccess(topic.id);

            view.createTopicSuccess(teamId, topic.id, topicTitle, isPublic);
        } catch (RetrofitError e) {
            view.dismissProgressWheel();
            final Response response = e.getResponse();
            int errorCode = response != null ? response.getStatus() : -1;
            topicCreateModel.trackTopicCreateFail(errorCode);
            if (response != null && response.getStatus() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.createTopicFailed(R.string.err_entity_duplicated_name);
            } else {
                view.createTopicFailed(R.string.err_entity_create);
            }
        } catch (Exception e) {
            topicCreateModel.trackTopicCreateFail(-1);
            view.dismissProgressWheel();
            view.createTopicFailed(R.string.err_entity_create);
        }
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }
}
