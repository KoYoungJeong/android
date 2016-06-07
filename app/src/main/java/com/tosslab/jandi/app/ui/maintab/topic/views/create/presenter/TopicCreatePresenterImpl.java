package com.tosslab.jandi.app.ui.maintab.topic.views.create.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.topic.views.create.model.TopicCreateModel;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;

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
                String distictId = TeamInfoLoader.getInstance().getMyId() + "-"
                        + TeamInfoLoader.getInstance().getTeamId();
                MixpanelMemberAnalyticsClient
                        .getInstance(JandiApplication.getContext(), distictId)
                        .trackCreatingEntity(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // TODO 응답값 매핑해서 DB 로 넣기

            view.dismissProgressWheel();

            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            topicCreateModel.trackTopicCreateSuccess(topic.id);

            view.createTopicSuccess(teamId, topic.id, topicTitle, isPublic);
        } catch (RetrofitException e) {
            view.dismissProgressWheel();
            int errorCode = e.getResponseCode();
            topicCreateModel.trackTopicCreateFail(errorCode);
            if (e.getStatusCode() == JandiConstants.NetworkError.DUPLICATED_NAME) {
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
