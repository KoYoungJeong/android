package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.model.TopicCreateModel;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicCreate;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import de.greenrobot.event.EventBus;

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
            Topic topic = topicCreateModel.createTopic(topicTitle, isPublic, topicDescriptionText, isAutojoin);
            topicCreateModel.addTopic(topic);

            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new RetrieveTopicListEvent());

            long teamId = TeamInfoLoader.getInstance().getTeamId();
            SprinklrTopicCreate.sendLog(topic.getId());

            view.dismissProgressWheel();
            view.createTopicSuccess(teamId, topic.getId(), topicTitle, isPublic);
        } catch (RetrofitException e) {
            view.dismissProgressWheel();
            int errorCode = e.getResponseCode();
            SprinklrTopicCreate.sendFailLog(errorCode);
            if (e.getStatusCode() == JandiConstants.NetworkError.DUPLICATED_NAME) {
                view.createTopicFailed(R.string.err_entity_duplicated_name);
            } else {
                view.createTopicFailed(R.string.err_entity_create);
            }
        } catch (Exception e) {
            SprinklrTopicCreate.sendFailLog(-1);
            view.dismissProgressWheel();
            view.createTopicFailed(R.string.err_entity_create);
        }
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }
}
